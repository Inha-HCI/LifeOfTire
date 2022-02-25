from ast import Num
import torch
import numpy as np
from torch.utils.data import Dataset,DataLoader
import torchvision.transforms as transforms
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
from tqdm import tqdm


import os
import math
from glob import glob
from PIL import Image
import pandas as pd
#rom tire_Dataset import TireDataset, get_imgset_mean_std


if __name__=='__main__':
    
    torch.hub._validate_not_a_forked_repo=lambda a,b,c: True
    
    learning_rate = 0.01
    
    model = torch.hub.load('pytorch/vision:v0.10.0', 'mobilenet_v2', pretrained=True)

    model.classifier[1] = nn.Linear(in_features=1280,out_features=7,bias=True)

    train_data = TireDataset('E:\\data\\Tire_data\\tire_data','E:\\data\Tire_data\\tire_result.xlsx')
    #print(get_imgset_mean_std(train_data))
    train_loader = DataLoader(train_data,batch_size=2,shuffle=True,num_workers=2,drop_last=True)
    
    optimizer = optim.Adam(model.parameters(), lr=learning_rate)

    # class_onehot = F.one_hot(torch.arange(0,7),num_classes= 7)

    for epoch in(range(1,101)):
        tqbar = tqdm(train_loader)

        
        for data in tqbar:
            _tmp = data['label']
            # label= class_onehot[_tmp]
            
            optimizer.zero_grad()
            
            output = model(data['image'])
            

            loss = F.cross_entropy(output,_tmp)
            loss.backward()
            optimizer.step()
            
            tqbar.set_postfix({'loss': loss.item()})
            # print(f"loss: {loss.item()}")
        torch.save(model, "seokchae.pt")
        

# input = torch.randn(3, 5, requires_grad=True)
# target = torch.empty(3, dtype=torch.long).random_(5)
# print(input, target)


# print(F.cross_entropy(input,target))

