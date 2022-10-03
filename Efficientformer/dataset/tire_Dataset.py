from pickletools import float8
from PIL import Image
import os
import torch
import pandas as pd
# from skimage import io, transform
import numpy as np
import matplotlib.pyplot as plt
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, utils
from glob import glob
from utils import split_img


#tire Dataset class
class TireDatasetSplit(Dataset):
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
            # self.transforms = transforms.Compose([transforms.Resize((512,672)),
            #                             transforms.ToTensor()])
            self.transforms = transforms.Compose([transforms.Resize((480,640)),
                            transforms.ToTensor()])
            
            # self.transforms = transforms.Compose([transforms.Resize((252,336)),
            #                             transforms.ToTensor(),
            #                             transforms.Normalize([meanR, meanG, meanB], [stdR, stdG, stdB])]
            # /self.label_transforms = transforms.Compose([transforms.ToTensor()])
        # label = torch.FloatTensor(label)
        
        self.img_paths = [] 
        self.label = []

        with open(excel_path,'rb') as f:
            label = pd.read_excel(f,)
        depth = label[['depth1', 'depth2', 'depth3','depth4', 'depth5', 'depth6', 'depth7', 'depth8', 'depth9', 'depth10','depth11', 'depth12']].apply(np.average,axis=1)
        label['depth'] = depth
        label['class'] = label['depth'].apply(round) #get Label -> average depth from depth points 

        label = label[['sid','class']] #sid:folder name ,class: label(average depth)


        for folder_name, cls in label[['sid','class']].values:
            f_path = os.path.join(root_path,str(folder_name))

            data_paths = glob(f'{f_path}/*.jpg')
            for img in data_paths:
                self.img_paths.append(img)
                self.label.append(cls)
    
    def __len__(self):
        return len(self.img_paths*(self.split_cnt**2))


    def __getitem__(self, idx):
        """_summary_

        Returns:
            dict{'image': tensor, 'label': int }
        """
        
        sub_idx = idx %(self.split_cnt**2)
        idx = idx//(self.split_cnt**2)
        label = self.label[idx]
        image = Image.open(self.img_paths[idx])
        image = np.array(image)
        images = split_img(image)
        # image = np.array(image)
        sample = {"image":images[sub_idx],'label':torch.tensor(label,dtype=float)}
        sample['image'] = self.transforms(sample['image'])
        print(label)
        return sample


class TireDataset(Dataset):
    def __init__(self,root_path, excel_path='F:\\data\Tire_data\\tire_result.xlsx',custom_transforms=None):
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
            # self.transforms = transforms.Compose([transforms.Resize((640,480)),
            #                 transforms.ToTensor(),
            #                 transforms.Normalize((0.485, 0.456, 0.406),(0.229, 0.224, 0.225))])
            self.transforms = transforms.Compose([transforms.Resize((640,480)),
                            transforms.ToTensor(),
                            ])
            
            # self.transforms = transforms.Compose([transforms.Resize((252,336)),
            #                             transforms.ToTensor(),
            #                             transforms.Normalize([meanR, meanG, meanB], [stdR, stdG, stdB])]
            # /self.label_transforms = transforms.Compose([transforms.ToTensor()])
        # label = torch.FloatTensor(label)
        self.img_paths = [] 
        self.label = []
        
        with open(excel_path,'rb') as f:
            label = pd.read_excel(f,)
        depth = label[['depth1', 'depth2', 'depth3','depth4', 'depth5', 'depth6', 'depth7', 'depth8', 'depth9', 'depth10','depth11', 'depth12']].apply(np.average,axis=1)

        label['depth'] = depth

        # label['class'] = label['depth'].apply(round ) #get Label -> average depth from depth points 
        result  = label[['depth']].apply(lambda x: round(x,2) )

        label['class'] = result

        label = label[['sid','class']] #sid:folder name ,class: label(average depth)


        for folder_name, cls in label[['sid','class']].values:
            
            f_path = os.path.join(root_path,str(int(folder_name)))
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

        label = self.label[idx]
        image = Image.open(self.img_paths[idx])
        # image = np.array(image)
        # image = np.array(image)
        sample = {"image":image,'label':torch.tensor(label,dtype=float)}
        sample['image'] = self.transforms(sample['image'])
        return sample


class TireDataset_Mask(Dataset):

    def __init__(self, root,size= (640,480) ,mode='train', custom_transform=None,device='cuda'):

        self.device = device
        self.mode = mode
        self.img_list = None
        if custom_transform:
            self.transforms = custom_transform

        else:
            self.transforms = transforms.Compose([transforms.Resize(size),
                            transforms.ToTensor()])

        self.load_image(root)
    

    def load_image(self,root):
        self.img_list = glob(os.path.join(root,self.mode,'**/*.jpg'),recursive=True)


        # self.images = [img_loader(img_path) for img_path in img_list]
        # self.labels = [label_maker(img_path) for img_path in img_list]

    def label_maker(self,path):
        label = os.path.dirname(path).split('/')[-1].split('_')[-1]
        return torch.tensor(float(label), dtype=torch.float32, device=self.device)


    def img_loader(self,path):
        img = Image.open(path)
        return self.transforms(img)


    def __len__(self):
        return len(self.img_list)
    

    def __getitem__(self, idx):
        
        path = self.img_list[idx]
        img = self.img_loader(path)
        label = self.label_maker(path)
        return img, label


        
        