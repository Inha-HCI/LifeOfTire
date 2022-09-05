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
    DG = tp.DependencyGraph().build_dependency( model, torch.randn(1, 3, 640, 480) )

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
            # prune_conv( m.conv1, 0.2 )
            # prune_conv( m.conv2, 0.2 )
        # if isinstance(m, models.resnet.Bottleneck) and blk_id <= 7:
            prune_conv( m.conv1, block_prune_probs[blk_id] )
            prune_conv( m.conv2, block_prune_probs[blk_id] )
            blk_id+=1
    return model

if __name__ == '__main__':
    # model = models.resnet18()
    # out_layer = nn.Sequential(
    #     nn.Dropout(p=0.5, inplace=True),
    #     nn.Linear(in_features=512, out_features=1, bias=True)
    # )
    # model.fc = out_layer

    model = models.resnet34()
    # out_layer = nn.Sequential(
    #     nn.Dropout(p=0.5, inplace=True),
    #     nn.Linear(in_features=2048, out_features=1, bias=True)
    # )
    # model.fc = out_layer

    # Informations of model parameters
    # model.load_state_dict(torch.load('./outputs/resnet18/resnet18_100epoch/80.pt')['model_state_dict'])
    # model.load_state_dict(torch.load('./outputs/resnet18/2022-08-09_00-12-34/2.pt')['model_state_dict'])
    model.cuda()
    print("Before Pruning")
    print(pms.summary(model, torch.zeros(1, 3, 640, 480).cuda()))
    model.eval()

    pruned_model = prune_model(model)
    torch.save(pruned_model, 'resnet18_80epoch_pruned.pt')
    print("After Pruning")
    print(pms.summary(pruned_model, torch.zeros(1, 3, 640, 480)))

    

    # Save android ptl file
    # pruned_model.eval()
    # example = torch.rand(1, 3, 640, 480)
    # traced_script_module = torch.jit.trace(pruned_model, example)
    # traced_script_module_optimized = optimize_for_mobile(traced_script_module)
    # traced_script_module_optimized._save_for_lite_interpreter("./prune_model_final.ptl")