import torch
import torchvision.models as modles
from torchvision import transforms
from PIL import Image
import torch.nn as nn
import pytorch_model_summary as pms
import time
from torch.utils.mobile_optimizer import optimize_for_mobile
from prune import prune_model

if __name__ == '__main__':
    # Model Architecture customizing
    model = modles.resnet18()
    out_layer = nn.Sequential(
        nn.Dropout(p=0.5, inplace=True),
        nn.Linear(in_features=512, out_features=1, bias=True)
    )
    model.fc = out_layer
    
    model = prune_model(model)

    print(pms.summary(model, torch.zeros(1, 3, 640, 480)))
    print('#'*50)
    

    # model = torch.load('./outputs/resnet18/2022-08-16_00-26-49/80.pt')['model_state_dict']
    # print(model)

    # Load weight
    model.load_state_dict(torch.load('./outputs/resnet18/resnet18_pruned_100epoch_new_data/80.pt')['model_state_dict'])
    # model.load_state_dict(torch.load('./outputs/resnet18/resnet18_100epoch/80.pt')['model_state_dict'])
    model.eval()

    print(pms.summary(model, torch.zeros(1, 3, 640, 480)))

    img_path = './tire3.jpg'

    transform = transforms.Compose([
                transforms.ToTensor(),
                transforms.Resize((640,480)),       # height, width
                transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
                ])

    st = time.time()
    img_data = Image.open(img_path)
    img_data = transform(img_data)
    img_data = torch.unsqueeze(img_data,0)
    output = model(img_data)
    print(output)
    et = time.time()

    # Inference time
    print(f"{et - st:.03f}")

    # example = torch.rand(1, 3, 640, 480)
    # traced_script_module = torch.jit.trace(model, example)
    # traced_script_module_optimized = optimize_for_mobile(traced_script_module)
    # traced_script_module_optimized._save_for_lite_interpreter("resnet18_100epoch_v2.ptl")