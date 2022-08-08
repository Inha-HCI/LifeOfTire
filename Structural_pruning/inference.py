import torch
import torchvision.models as modles
from torchvision import transforms
from PIL import Image
import torch.nn as nn
import time
from torch.utils.mobile_optimizer import optimize_for_mobile

if __name__ == '__main__':
    model = modles.resnet18()
    out_layer = nn.Sequential(
        nn.Dropout(p=0.5, inplace=True),
        nn.Linear(in_features=512, out_features=1, bias=True)
    )
    model.fc = out_layer

    model.load_state_dict(torch.load('./outputs/resnet18/2022-08-09_00-12-34/2.pt')['model_state_dict'])
    model.eval()
    # print(weight)
    # model.load_state_dict(weight['model_state_dict'])
    # model.load_state_dict(weight)

    print(model)
    # img_path = '../Dataset/tire_data/tire_data/2021111013477516/2021-11-10-13-47-39-333.jpg'
    img_path = './tire.jpg'
    
    # transform = transforms.Compose([transforms.Resize((512,672)),
    #                                     transforms.ToTensor()])

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
    et = time.time()

    print(f"{et - st:.03f}")
    print(output)

    example = torch.rand(1, 3, 640, 480)
    # scriptedm = torch.jit.script(model)
    # torch.jit.save(scriptedm, "reres.pt")
    traced_script_module = torch.jit.trace(model, example)
    traced_script_module_optimized = optimize_for_mobile(traced_script_module)
    traced_script_module_optimized._save_for_lite_interpreter("resnet_v4.ptl")