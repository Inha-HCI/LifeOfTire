import torch
import torch_pruning as tp
import pytorch_model_summary as pms
import torchvision.models as models
import torch.nn as nn
from torch.utils.mobile_optimizer import optimize_for_mobile
from PIL import Image
from torchvision import transforms

def prune_model(model):
    model.cpu()
    DG = tp.DependencyGraph().build_dependency( model, torch.randn(1, 3, 32, 32) )
    def prune_conv(conv, amount=0.2):
        strategy = tp.strategy.L1Strategy()
        pruning_index = strategy(conv.weight, amount=amount)
        plan = DG.get_pruning_plan(conv, tp.prune_conv_out_channel, pruning_index)
        plan.exec()
    
    block_prune_probs = [0.1, 0.1, 0.2, 0.2, 0.2, 0.2, 0.3, 0.3]
    blk_id = 0
    # print(list(model.modules()))
    
    for m in model.modules():
        if isinstance( m, models.resnet.BasicBlock ):
            prune_conv( m.conv1, block_prune_probs[blk_id] )
            prune_conv( m.conv2, block_prune_probs[blk_id] )
            blk_id+=1
    return model

if __name__ == '__main__':
    # Model architecture customizing
    model = models.resnet18(pretrained=True)
    # model = torch.hub.load('pytorch/vision:v0.10.0', 'resnet18', pretrained=True)
    out_layer = nn.Sequential(
        nn.Dropout(p=0.5, inplace=True),
        nn.Linear(in_features=512, out_features=1, bias=True)
    )
    model.fc = out_layer

    transform = transforms.Compose([
                transforms.ToTensor(),
                transforms.Resize((640,480)),       # height, width
                transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
                ])

    img_data = transform(Image.open('./tire.jpg'))
    img_data = torch.unsqueeze(img_data,0)
    print(model(img_data))
    exit()

    # Model weightfile load
    weight = torch.load('./outputs/resnet18/2022-08-08_20-57-21/6.pt')
    model.load_state_dict(weight['model_state_dict'])
    model.eval()
    transform = transforms.Compose([transforms.Resize((480,640)),
                                        transforms.ToTensor(),
                                        transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
                                        ])
    img_data = transform(Image.open('./tire.jpg'))
    img_data = torch.unsqueeze(img_data,0)
    print(model(img_data))

    example = torch.rand(1, 3, 640, 480)
    traced_script_module = torch.jit.trace(model, example)
    traced_script_module_optimized = optimize_for_mobile(traced_script_module)
    # traced_script_module_optimized._save_for_lite_interpreter("./android_model.ptl")
    traced_script_module_optimized._save_for_lite_interpreter("resnet18.ptl")


    exit()
    # Informations of model parameters
    model.cuda()
    print("Before Pruning")
    print(pms.summary(model, torch.zeros(1, 3, 512, 672).cuda()))
    model.eval()

    pruned_model = prune_model(model)
    torch.save(pruned_model, 'pruned.pt')
    print("After Pruning")
    print(pms.summary(pruned_model, torch.zeros(1, 3, 512, 672)))

    transform = transforms.Compose([transforms.Resize((480,640)),
                                        transforms.ToTensor()])

    img_data = transform(Image.open('./tire.jpg'))
    img_data = torch.unsqueeze(img_data,0)
    print(pruned_model(img_data))
    pruned_model.eval()

    example = torch.rand(1, 3, 640, 480)
    traced_script_module = torch.jit.trace(pruned_model, example)
    traced_script_module_optimized = optimize_for_mobile(traced_script_module)
    # traced_script_module_optimized._save_for_lite_interpreter("./android_model.ptl")
    traced_script_module_optimized._save_for_lite_interpreter("./prune_model_v2.ptl")