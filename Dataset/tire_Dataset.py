from PIL import Image
import os
import torch
import pandas as pd
# from skimage import io, transform
import numpy as np
import matplotlib.pyplot as plt
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, utils
import torchvision
import math
from tqdm import tqdm
from glob import glob


#img 자르기 
def split_img(img,split_cnt = 3):
    w,h,_ = np.shape(img)
    init_w = w//split_cnt
    init_h = h//split_cnt
    pices = []
    for i in range(split_cnt):
        for j in range(split_cnt):
            pices.append(Image.fromarray(img[init_w *i:init_w*(i+1),init_h *j:init_h*(j+1)]))

    return pices


# caculate Datasets's RGB std & mean  for nomalization 
def get_imgset_mean_std(train_ds):
    """_summary_

    Args:
        train_ds (torch.util.data.Dataset): only resize and totensor transform dataset

    Returns:
        _type_: (meanR,meanG,meanB), (stdR,stdG,stdB)
    """
    meanRGB = [np.mean(x['image'].numpy(), axis=(1,2)) for x in tqdm(train_ds)]
    stdRGB = [np.std(x['image'].numpy(), axis=(1,2)) for x in tqdm(train_ds)]
    meanR = np.mean([m[0] for m in meanRGB])
    meanG = np.mean([m[1] for m in meanRGB])
    meanB = np.mean([m[2] for m in meanRGB])

    stdR = np.mean([s[0] for s in stdRGB])
    stdG = np.mean([s[1] for s in stdRGB])
    stdB = np.mean([s[2] for s in stdRGB])
    
    
    print(meanR,meanG,meanB)
    print(stdR,stdG,stdB)
    return (meanR,meanG,meanB), (stdR,stdG,stdB)


#tire Dataset class
class TireDataset(Dataset):
    def __init__(self,root_path, excel_path,custom_transforms=None):
        """_summary_

        Args:
            root_path (str): data root folder
            excel_path (str): tire_data label excel-> 'tire_result.xlsx' path
            custom_transforms (torchvision.transformers, optional): custom transforms method 

        """
        self.split_cnt = 3
        if custom_transforms:
            self.transforms = custom_transforms


        else:
            #org:3024 x 4032 default transforms
            self.transforms = transforms.Compose([transforms.Resize((252,336)),
                                        transforms.ToTensor()])
            # self.transforms = transforms.Compose([transforms.Resize((252,336)),
            #                             transforms.ToTensor(),
            #                             transforms.Normalize([meanR, meanG, meanB], [stdR, stdG, stdB])]

        # label = torch.FloatTensor(label)
        self.img_paths = [] 
        self.label = []

        with open(excel_path,'rb') as f:
            label = pd.read_excel(f,)
        depth = label[['depth1', 'depth2', 'depth3','depth4', 'depth5', 'depth6', 'depth7', 'depth8', 'depth9', 'depth10','depth11', 'depth12']].apply(np.average,axis=1)
        label['depth'] = depth
        label['class'] = label['depth'].apply(math.trunc) #get Label -> average depth from depth points 
        label = label[['sid','class']] #sid:folder name ,class: label(average depth)
        


        for folder_name, cls in tqdm(label[['sid','class']].values):
            f_path = os.path.join(root_path,str(folder_name))

            data_paths = glob(f'{f_path}/*.jpg')
            for img in data_paths:
                self.img_paths.append(img)
                self.label.append(cls)
    
    def __len__(self):
        return len(self.img_paths)



    def __getitem__(self, idx):
        """_summary_


        Returns:
            dict{'image': tensor, 'label': int }
        """

        sub_idx = idx %(self.split_cnt*self.split_cnt)
        idx = idx//self.split_cnt
        label = self.label[idx]
        image = Image.open(self.img_paths[idx])
        image = np.array(image)
        images = split_img(image)
        # image = np.array(image)
        sample = {"image":images[sub_idx],'label':label}
        sample['image'] = self.transforms(sample['image'])

        return sample

if __name__=='__main__':
    # train_data = TireDataset('F:\\data\\Tire_data\\3차','./Dataset/tire_result.xlsx')

    # train_loader = DataLoader(dataset = train_data, batch_size=2, shuffle=True, num_workers= 2, drop_last=True)

    # for n, img in enumerate(train_loader):
    #     print(img['image'].shape)
    #     print(img['label'].shape)

    # data_transformer = transforms.Compose([transforms.ToTensor()])
    
    train_data = TireDataset('F:\\data\Tire_data\\tire_data','./Dataset/tire_result.xlsx') # Data path
    get_imgset_mean_std(train_data)