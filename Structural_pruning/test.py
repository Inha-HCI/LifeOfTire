import torch
import torchvision.transforms as transforms
import numpy as np


transform = transforms.Compose([
    transforms.ToTensor(),
    transforms.Resize((640, 480)),
    transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225)),
    ])

input = np.array([[[50.0]], [[150.0]], [[200.0]]], dtype=np.uint8)
print(input.shape)
output = transform(input)
print(output)