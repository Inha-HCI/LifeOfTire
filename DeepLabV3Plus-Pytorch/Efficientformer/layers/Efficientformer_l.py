import torch
import torch.nn as nn
import torch.nn.functional as F
import itertools
from utils.timmfunc import DropPath


class PatchEmbedding(nn.Module):

    def __init__(self, patch_size=16, stride=16, padding=0,
                in_chans=3,embed_dim=768):
        super().__init__()
        self.proj = nn.Conv2d(in_chans,embed_dim, kernel_size=patch_size,
                    stride=(stride,stride),padding=(padding,padding))
        self.norm = nn.BatchNorm2d(embed_dim)


    def forward(self, x):
        x = self.proj(x)
        x = self.norm(x)
        return x


class Flat(nn.Module):

    def __init__(self, ):
        super().__init__()

    def forward(self, x):
        x = x.flatten(2).transpose(1, 2)
        return x


class StemConv(nn.Module):

    def __init__(self,in_chs, out_chs):
        super(StemConv,self).__init__()
        self.conv1 = nn.Conv2d(in_chs, out_chs, kernel_size=2, stride=2, padding=0)
        self.batch1 = nn.BatchNorm2d(out_chs)
        self.act1 = nn.ReLU()
        # self.conv2 = nn.Conv2d(out_chs//2, out_chs,kernel_size=2, stride=2, padding=0)
        # self.batch2 = nn.BatchNorm2d(out_chs)
        # self.act2 = nn.ReLU()


    def forward(self, x):
        x = self.act1(self.batch1(self.conv1(x)))
        # x = self.act2(self.batch2(self.conv2(x)))
        return x



#  pool_size, stride=1, padding=pool_size // 2,

class MB4D(nn.Module):

    def __init__(self, in_chs, h_chs,out_chs):
        super(MB4D,self).__init__()
        self.pool = nn.AvgPool2d(kernel_size=3,stride=1,padding=1)
        self.conv1 = nn.Conv2d(in_chs,h_chs,stride=1, kernel_size=1)
        self.batch1 = nn.BatchNorm2d(h_chs)
        self.act = nn.GELU()
        self.conv2 = nn.Conv2d(h_chs,out_chs,stride=1,kernel_size=3,padding=1)
        self.batch2 = nn.BatchNorm2d(out_chs)
    

    def forward(self, x):
        x_h = self.pool(x) + x
        out = self.batch1(self.conv1(x_h))
        out = self.act(out)
        out =  self.batch2(self.conv2(out))
        out = out + x_h

        return out



class Attention(torch.nn.Module):
    def __init__(self, dim=384, key_dim=32, num_heads=8,
                 attn_ratio=4,
                 resolution=7):
        super().__init__()
        self.num_heads = num_heads
        self.scale = key_dim ** -0.5
        self.key_dim = key_dim
        self.nh_kd = nh_kd = key_dim * num_heads
        self.d = int(attn_ratio * key_dim)
        self.dh = int(attn_ratio * key_dim) * num_heads
        self.attn_ratio = attn_ratio
        h = self.dh + nh_kd * 2

        self.qkv = nn.Linear(dim, h)
        self.proj = nn.Linear(self.dh, dim)

        points = list(itertools.product(range(resolution), range(resolution)))
        N = len(points)
        attention_offsets = {}
        idxs = []
        for p1 in points:
            for p2 in points:
                offset = (abs(p1[0] - p2[0]), abs(p1[1] - p2[1]))
                if offset not in attention_offsets:
                    attention_offsets[offset] = len(attention_offsets)
                idxs.append(attention_offsets[offset])
        self.attention_biases = torch.nn.Parameter(
            torch.zeros(num_heads, len(attention_offsets)))
        self.register_buffer('attention_bias_idxs',
                             torch.LongTensor(idxs).view(N, N))

    @torch.no_grad()
    def train(self, mode=True):
        super().train(mode)
        if mode and hasattr(self, 'ab'):
            del self.ab
        else:
            self.ab = self.attention_biases[:, self.attention_bias_idxs]

    def forward(self, x):  # x (B,N,C)
        B, N, C = x.shape
        qkv = self.qkv(x)
        q, k, v = qkv.reshape(B, N, self.num_heads, -1).split([self.key_dim, self.key_dim, self.d], dim=3)

        

        q = q.permute(0, 2, 1, 3)
        k = k.permute(0, 2, 1, 3)
        v = v.permute(0, 2, 1, 3)


     

        attn = (
                (q @ k.transpose(-2, -1)) * self.scale
                # +
                # (self.attention_biases[:, self.attention_bias_idxs]
                #  if self.training else self.ab)
        )
        attn = attn.softmax(dim=-1)
        x = (attn @ v).transpose(1, 2).reshape(B, N, self.dh)
        x = self.proj(x)
        return x


class MB3D(nn.Module):

    def __init__(self, dim, mlp_ratio=4.,
                 act_layer=nn.GELU, norm_layer=nn.LayerNorm,
                 drop=0., drop_path=0.,
                 use_layer_scale=True, layer_scale_init_value=1e-5):

        super().__init__()

        self.norm1 = norm_layer(dim) 
        self.token_mixer = Attention(dim)
        self.norm2 = norm_layer(dim)
        mlp_hidden_dim = int(dim * mlp_ratio)
        self.mlp = LinearMlp(in_features=dim, hidden_features=mlp_hidden_dim,
                             act_layer=act_layer, drop=drop)

        self.drop_path = DropPath(drop_path) if drop_path > 0. \
            else nn.Identity()
        self.use_layer_scale = use_layer_scale
        if use_layer_scale:
            self.layer_scale_1 = nn.Parameter(
                layer_scale_init_value * torch.ones((dim)), requires_grad=True)
            self.layer_scale_2 = nn.Parameter(
                layer_scale_init_value * torch.ones((dim)), requires_grad=True)

    def forward(self, x):
        if self.use_layer_scale:
            x = x + self.drop_path(
                self.layer_scale_1.unsqueeze(0).unsqueeze(0)
                * self.token_mixer(self.norm1(x)))
            x = x + self.drop_path(
                self.layer_scale_2.unsqueeze(0).unsqueeze(0)
                * self.mlp(self.norm2(x)))

        else:
            x = x + self.drop_path(self.token_mixer(self.norm1(x)))
            x = x + self.drop_path(self.mlp(self.norm2(x)))
        return x

class LinearMlp(nn.Module):
    """ MLP as used in Vision Transformer, MLP-Mixer and related networks
    """

    def __init__(self, in_features, hidden_features=None, out_features=None, act_layer=nn.GELU, drop=0.):
        super().__init__()
        out_features = out_features or in_features
        hidden_features = hidden_features or in_features

        self.fc1 = nn.Linear(in_features, hidden_features)
        self.act = act_layer()
        self.drop1 = nn.Dropout(drop)
        self.fc2 = nn.Linear(hidden_features, out_features)
        self.drop2 = nn.Dropout(drop)

    def forward(self, x):
        x = self.fc1(x)
        x = self.act(x)
        x = self.drop1(x)
        x = self.fc2(x)
        x = self.drop2(x)
        return x