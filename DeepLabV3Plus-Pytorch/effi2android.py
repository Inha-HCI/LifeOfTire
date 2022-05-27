import torchvision.models as models
import torchvision
import torch
import torch.nn as nn

from torch.utils.mobile_optimizer import optimize_for_mobile

if __name__ == '__main__':

    # Model Initializing
    model = models.efficientnet_b7(pretrained=True)
    out_layer = nn.Sequential(
        nn.Dropout(p=0.5, inplace=True),
        nn.Linear(in_features=2560, out_features=1, bias=True)
    )
    model.classifier = out_layer
    model.load_state_dict(torch.load('./efficient_reg.pt')['model_state_dict'])
    model.eval()


    # Convert weight file for android
    scripted_module = torch.jit.script(model)
    optimized_scripted_module = optimize_for_mobile(scripted_module)

    # Export full jit version model (not compatible with lite interpreter)
    # scripted_module.save("deeplabv3_scripted.pt")
    # Export lite interpreter version model (compatible with lite interpreter)
    # scripted_module._save_for_lite_interpreter("deeplabv3_scripted.ptl")
    # using optimized lite interpreter model makes inference about 60% faster than the non-optimized lite interpreter model, which is about 6% faster than the non-optimized full jit model
    optimized_scripted_module._save_for_lite_interpreter("efficient_scripted_optimized.ptl")