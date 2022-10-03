from PIL import Image
import numpy as np
from tqdm import tqdm
from glob import glob
import os
from sklearn.model_selection import train_test_split
import shutil

from itertools import repeat

#make validation & train set from dataset
def tire_set_split_set_dir(path):
    org_path = os.path.join(path,'tire_data')
    dirs = os.listdir(path=org_path)
    test_path = os.path.join(path,'test')
    train_path = os.path.join(path,'train')
    for dir in tqdm(dirs):
        os.makedirs(os.path.join(test_path,dir))
        os.makedirs(os.path.join(train_path,dir))
        imgs = glob(os.path.join(org_path,dir,'*.jpg'))
        train_set,test_set = train_test_split(imgs,test_size=0.2, random_state=10)
        for img in train_set:
            base_n = os.path.basename(img)
            tar_img = os.path.join(train_path,dir, base_n)
            shutil.copy(img, tar_img)

        for img in test_set:
            base_n = os.path.basename(img)
            tar_img = os.path.join(test_path,dir, base_n)
            shutil.copy(img, tar_img)



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
    
    

    return (meanR,meanG,meanB), (stdR,stdG,stdB)

def to_ntuple(x,n):
    return tuple(repeat(x, n))

if __name__=='__main__':
    root = 'F:\\data\Tire_data'
    tire_set_split_set_dir(root)