from torchvision.transforms.functional import normalize
import torch.nn as nn
import numpy as np
import os 
from PIL import Image

def denormalize(tensor, mean, std):
    mean = np.array(mean)
    std = np.array(std)

    _mean = -mean/std
    _std = 1/std
    return normalize(tensor, _mean, _std)

class Denormalize(object):
    def __init__(self, mean, std):
        mean = np.array(mean)
        std = np.array(std)
        self._mean = -mean/std
        self._std = 1/std

    def __call__(self, tensor):
        if isinstance(tensor, np.ndarray):
            return (tensor - self._mean.reshape(-1,1,1)) / self._std.reshape(-1,1,1)
        return normalize(tensor, self._mean, self._std)

def set_bn_momentum(model, momentum=0.1):
    for m in model.modules():
        if isinstance(m, nn.BatchNorm2d):
            m.momentum = momentum

def fix_bn(model):
    for m in model.modules():
        if isinstance(m, nn.BatchNorm2d):
            m.eval()

def mkdir(path):
    if not os.path.exists(path):
        os.mkdir(path)



## make masked image for Efficientformer input LMH (22.10.03)
def image_mask_filtered(img,pred):
    """_summary_ make masked_image from mask and image

    Args:
        img (Image): _description_
        pred (np.array): _description_ H, W, 3

    Returns:
        _type_: _description_
    """
    sample = np.array(img)
    sample = sample[:,:,:3] # 채널 3음로변경하는 부분
    mask = np.expand_dims(pred, axis=2)
    mask_3ch = np.repeat(mask,[3],axis=2)
    masked_image = Image.fromarray(np.where(mask_3ch==1, img,0).astype(np.uint8))
    return masked_image
