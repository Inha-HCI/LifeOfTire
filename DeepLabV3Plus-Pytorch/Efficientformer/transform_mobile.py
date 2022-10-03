from xmlrpc.client import boolean
import torchvision.models as models
import torch.nn as nn
from torch.utils.data import DataLoader,random_split
from torchvision import transforms
from dataset.tire_Dataset import TireDataset, TireDatasetSplit
from tqdm import tqdm 
from tqdm import trange
import torch.optim as optim 
import torch
from PIL import Image
from glob import glob
import pandas as pd

from torch.utils.mobile_optimizer import optimize_for_mobile
from datetime import datetime
import argparse
import yaml
import os
from main import build_model

from models.Efficient import Efficientformer

def parse_args():
    parser = argparse.ArgumentParser(description='Train model')
    parser.add_argument("-cfg",'--config',type=str,required=True, help="path to config file")
    # parser.add_argument("--data", type=str, required=True, help='path to dataset')
    parser.add_argument('--pt_path',type=str,help='path to model weight file')
    args = parser.parse_args()
    return args


def load_model(args):
    config = yaml.safe_load(open(args.config, "r"))
    model = build_model(config)
    # model = model
    model.load_state_dict(torch.load(args.pt_path)['model_state_dict'])
    model.eval()
    return model


def transform_mobile_format(model):
    scripted_module = torch.jit.script(model)
    optimized_scripted_module = optimize_for_mobile(scripted_module)
    # Export full jit version model (not compatible with lite interpreter)
    scripted_module.save("Efficientnet_tire_depth.pt")
    # Export lite interpreter version model (compatible with lite interpreter)
    scripted_module._save_for_lite_interpreter("Efficientnet_tire_depth.ptl")
    # using optimized lite interpreter model makes inference about 60% faster than the non-optimized lite interpreter model, which is about 6% faster than the non-optimized full jit model
    optimized_scripted_module._save_for_lite_interpreter("Efficientnet_scripted_optimized.ptl")


if __name__ == '__main__':
    args = parse_args()
    # model = load_model(args)
    pre_trained = torch.load('./outputs/Efficientformer/60.pt')


    model = Efficientformer(3,1).cuda()
    model.state_dict = pre_trained.state_dict()
    model.eval()
    transform_mobile_format(model)