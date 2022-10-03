import torch
import torch.nn as nn
import torch.nn.functional as F

from layers.Efficientformer_l import *

chs_out = []
MB4d_chs = []

class OutLayer(nn.Module):
    def __init__(self,in_c,hidden_c):
        super().__init__()
        self.last_conv = nn.Conv1d(in_c,in_c//2,kernel_size=3,stride=3)
        self.last_conv2 = nn.Conv1d(in_c//2,in_c//4,kernel_size=2,stride=2)
        self.linear = nn.Linear(hidden_c,hidden_c//2)
        self.act = nn.GELU()
        self.proj = nn.Linear(hidden_c//2,1) 


    def forward(self,x):
        x= self.last_conv(x)
        x = self.last_conv2(x)

        out = self.linear(x.flatten(-2))
        out = self.act(out)
        out = self.proj(out)


        return out



class Efficientformer(nn.Module):

    def __init__(self,in_chs, out_chs):
        super().__init__()
        stem_out = 3
        mb4d_n = 2
        down_patch_size=3
        down_stride=2
        down_pad=1
        embed_dims = []
        self.s_conv1  = StemConv(in_chs, stem_out)
        self.s_conv2  = StemConv(stem_out, stem_out)
        
        self.mb_4d = MB4D(3,168,3)
        self.embed = PatchEmbedding(10,10)
        self.flatten = Flat()
        self.mb_3d = MB3D(768)
        self.s_conv3 = StemConv(stem_out, stem_out)
        self.out_layer = OutLayer(192,6144)

        
        blocks = []
        # for i in range(mb4d_n):
        #     blocks.append(MB4D(MB4d_chs[i],MB4d_chs[i+1]),PatchEmbedding(in_chs=MB4d_chs[i+1]))
        
        # self.mb4d_net = nn.Sequential(*blocks)
        # self.mb4d_last = MB4D(MB4d_chs[mb4d_n],MB4d_chs[mb4d_n+1])
        # self.flatten_l = Flat()

        # self.embedding = PatchEmbedding(patch_size=down_patch_size, stride=down_stride,
        #                 padding=down_pad,
        #                 in_chans=embed_dims[i], embed_dim=embed_dims[i + 1])
        

    def forward(self,x):

        x = self.s_conv1(x)
        x = self.s_conv2(x)
        x = self.mb_4d(x)
        x = self.embed(x)
        x = self.flatten(x)
        out = self.mb_3d(x)

        out = self.out_layer(out)

        return out


        