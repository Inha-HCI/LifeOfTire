import torch
import numpy as np
from torch.utils.data import Dataset,DataLoader
import torchvision.transforms as transforms
import torch.nn as nn
from tqdm import tqdm


import os
import math
from glob import glob
from PIL import Image
import pandas as pd


from tire_Dataset import TireDataset, get_imgset_mean_std

if __name__=='__main__':

    model = torch.hub.load('pytorch/vision:v0.10.0', 'mobilenet_v2', pretrained=True)
    model.classifier[1] = nn.Linear(in_features=1200,out_features=7,bias=True)

    train_data = TireDataset('F:\\data\Tire_data\\tire_data','F:\\data\Tire_data\\tire_result.xlsx')
    print(get_imgset_mean_std(train_data))
    train_loader = DataLoader(train_data,batch_size=1,shuffle=True,num_workers=2,drop_last=True)



    for data in train_loader:
        print(data['image'].size())
        print(data['label'])
        exit()