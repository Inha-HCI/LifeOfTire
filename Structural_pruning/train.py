from xmlrpc.client import boolean
import torch_pruning as tp
import torchvision.models as models
import torch.nn as nn
from torch.utils.data import DataLoader,random_split
from torchvision import transforms
from dataset.tire_Dataset import TireDataset, TireDatasetSplit, TireDatasetMask
from tqdm import tqdm 
from tqdm import trange
import torch.optim as optim 
import torch
from PIL import Image
from glob import glob
import pandas as pd
import wandb


from datetime import datetime
import argparse
import yaml
import os
import time

os.environ['CUDA_LAUNCH_BLOCKING'] = "1"
os.environ["CUDA_VISIBLE_DEVICES"] = "0"


def make_exp_folder(config):
    """_summary_

    Args:
        config (_type_): config data

    Returns:
        str: path of output dir
    """
    model_dir = os.path.join(os.getcwd(),'outputs',config['model'])
    exp_dir = os.path.join(model_dir,datetime.now().strftime('%Y-%m-%d_%H-%M-%S'))
    os.makedirs(exp_dir, exist_ok=True)
    return exp_dir


def parse_args():
    parser = argparse.ArgumentParser(description='Train model')
    parser.add_argument("-cfg",'--config',type=str,required=True, help="path to config file")
    parser.add_argument('--name',type=str, required=True, help='wandb name')
    parser.add_argument('--pruning', default=False, help='Do pruning', action='store_true')
    # parser.add_argument("--data", type=str, required=True, help='path to dataset')
    parser.add_argument('--eval',  default=False, help='model mode eval',action='store_true')
    parser.add_argument('--infer', default=False,help='model model infer', action='store_true')
    parser.add_argument('--img_path',type=str, help='Path of the image to be inferred')
    parser.add_argument('-lr','--lr', default=0.0001, help='learning rate')
    parser.add_argument('--epochs',type=int, default=200, help='Number of epochs')
    parser.add_argument('-bt','--batch_size',type=int ,default=2, help='data batch size')
    parser.add_argument('--check_iter',type=int, default=1,help='Eval and save interval')
    parser.add_argument('--pt_path',type=str,help='path to model weight file')
    args = parser.parse_args()
    return args


def build_dataset(configs,mode='train'):
    if mode =='train':
        root_path='../Dataset/tire_data/tire_data/train'

    else:
        root_path='../Dataset/tire_data/tire_data/test'

    if configs['Dataset'] =='TireSplit':
        # dataset = TireDatasetSplit(root_path,'F:\\data\Tire_data\\tire_result.xlsx')
        dataset = TireDatasetSplit(root_path,'../Dataset/tire_data/tire_result.xlsx')
    elif configs['Dataset'] == 'Tire':
        dataset = TireDataset(root_path,'../Dataset/tire_data/tire_result.xlsx', mode)

    elif configs['Dataset'] == 'Tire_mask':
        dataset = TireDatasetMask(root_path)

    assert dataset != None,'Please set dataset'
    return dataset


def build_dataloader(args,dataset):

    train_loader = DataLoader(dataset,batch_size=args.batch_size,shuffle=True,num_workers=8,drop_last=True)
    return train_loader


def build_model(configs):
    if configs['model'] =='Efficientnet':
        model = models.efficientnet_b7(pretrained=True)

        out_layer = nn.Sequential(
            nn.Dropout(p=0.5, inplace=True),
            nn.Linear(in_features=2560, out_features=1, bias=True)
        )
        model.classifier = out_layer

    if configs['model'] =='resnet18':
        # model = models.efficientnet_b7(pretrained=True)
        model = models.resnet18(pretrained=True)

        out_layer = nn.Sequential(
            nn.Dropout(p=0.5, inplace=True),
            nn.Linear(in_features=512, out_features=1, bias=True)
        )
        # model.classifier = out_layer
        model.fc = out_layer

    if configs['model'] =='resnet34':
        model = models.resnet34(pretrained=True)

        out_layer = nn.Sequential(
            nn.Dropout(p=0.5, inplace=True),
            nn.Linear(in_features=512, out_features=1, bias=True)
        )
        # model.classifier = out_layer
        model.fc = out_layer

    if args.pruning:
        print("Executing Pruning")
        model = prune_model(model)

    assert model != None,'Please setting model' 
    return model


def build_optim(model,args):

    optimizer = optim.Adam(model.parameters(),lr = args.lr)
    return optimizer


def build_criterion(configs):
    if configs['criterian'] == 'MSE':
        return nn.MSELoss()
    else:
        return nn.MSELoss()

def prune_model(model):
    model.cpu()
    DG = tp.DependencyGraph().build_dependency( model, torch.randn(1, 3, 640, 480) )
    def prune_conv(conv, amount=0.2):
        strategy = tp.strategy.L1Strategy()
        pruning_index = strategy(conv.weight, amount=amount)
        plan = DG.get_pruning_plan(conv, tp.prune_conv_out_channel, pruning_index)
        plan.exec()
    
    block_prune_probs = [0.1, 0.1, 0.2, 0.2, 0.2, 0.2, 0.3, 0.3]
    blk_id = 0
    # print(list(model.modules()))
    
    for m in model.modules():
        if isinstance( m, models.resnet.BasicBlock ):
            prune_conv( m.conv1, block_prune_probs[blk_id] )
            prune_conv( m.conv2, block_prune_probs[blk_id] )
            blk_id+=1
    return model

def train(args):
    config = yaml.safe_load(open(args.config, "r"))
    exp_dir = make_exp_folder(config)

    model = build_model(config)
    model = model.cuda()
    # totalset = build_dataset(config)
    train_set = build_dataset(config,mode='train')
    val_set = build_dataset(config,mode='test')
    # train_set,val_set = random_split(totalset,[int(len(totalset)*0.8),len(totalset)-int(len(totalset)*0.8)],generator=torch.Generator().manual_seed(42))
    train_loader = build_dataloader(args,train_set)
    val_loader = build_dataloader(args,val_set)
    optimizer = build_optim(model,args)
    criterian = build_criterion(config)

    epoch_bar = tqdm(range(args.epochs),desc='epoch loop')
    # in_data = next(iter(train_loader))
    model.train()
    wandb.watch(model)

    for epoch in epoch_bar:
        total_loss = train_once(model,train_loader,criterian,optimizer)

        total_loss = total_loss / len(train_loader)
        wandb.log({"train_avg_loss": total_loss})        

        epoch_bar.set_postfix_str({'avg_loss': total_loss})

        if (epoch+1) % args.check_iter == 0:

            eval_once(epoch,model, val_loader,criterian)
            torch.save(
                {
                    "model_state_dict":model.state_dict()
                },
                os.path.join(exp_dir,f'{epoch+1}.pt')
            )
            model.train()
    

def train_once(model,train_loader,criterian,optimizer):
    total_loss= 0
    dataloader_bar = tqdm(train_loader,desc='data loop',leave=False)
    for image, label in dataloader_bar:
        # output = model(in_data['image'].cuda())
        output = model(image.cuda())
        # loss = criterian(output.to(torch.float32),in_data['label'].to(torch.float32).cuda().view(args.batch_size,1))
        loss = criterian(output.to(torch.float32), label.cuda().unsqueeze(1))
        # loss = loss.sum()
        total_loss += loss.item()
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        # dataloader_bar.set_postfix({'loss':loss})

    return total_loss

def eval(args):
    print('start evaluation')
    config = yaml.safe_load(open(args.config, "r"))
    model = build_model(config)
    model = model.cuda()
    model.load_state_dict(torch.load(args.pt_path)['model_state_dict'])
    model.eval()
    
    # totalset = build_dataset(config)
    val_set = build_dataset(config,mode='test')
    val_loader = build_dataloader(args,val_set)
    criterian = build_criterion(config)
    avg_loss = eval_once(0,model,val_loader,criterian,mode='eval')
    print(f'avg_loss: {avg_loss}')
    # in_data = next(iter(train_loader))


def infer(args):
    config = yaml.safe_load(open(args.config, "r"))
    model = build_model(config)
    # model = model
    model.load_state_dict(torch.load(args.pt_path)['model_state_dict'])
    model.eval()
    transform = transforms.Compose([transforms.Resize((512,672)),
                                    transforms.ToTensor(),
                                    transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))])
    img_data = Image.open(args.img_path)
    img_data = transform(img_data)
    img_data = torch.unsqueeze(img_data,0)
    output = model(img_data)
    return output


def infer_from_dir(args):
    imgs = glob(os.path.join(args.img_path,'*.jpg'))
    config = yaml.safe_load(open(args.config, "r"))
    model = build_model(config)
    # model = model
    model.load_state_dict(torch.load(args.pt_path)['model_state_dict'])
    model.eval()
    transform = transforms.Compose([transforms.Resize((512,672)),
                                        transforms.ToTensor()])
    result =[]
    for img in tqdm(imgs):
        img_data = Image.open(img)
        img_data = transform(img_data)
        img_data = torch.unsqueeze(img_data,0)
        output = model(img_data)
        base_n = os.path.basename(img)
        output = output.detach().numpy()[0]
        result.append([base_n,output])
    csv_data = pd.DataFrame(result, columns=['file','predict'])
    csv_data.to_csv('infer_result.csv')



def eval_once(epoch,model,val_loader,criterian,mode='train'):
    print(f'start evaluation epoch: {epoch}')
    total_loss= 0
    dataloader_bar = tqdm(val_loader,desc='validation loop')
    model.eval()
    csv_list = []
    for image, label in dataloader_bar:
        with torch.no_grad():
            output = model(image.cuda())
            
            # loss = criterian(output.to(torch.float32).type(torch.FloatTensor),label.to(torch.float32).type(torch.FloatTensor).view(args.batch_size,1))
            loss = criterian(output.to(torch.float32), label.cuda().unsqueeze(1))

        if mode =='eval':
            label = label
 
        # loss = loss.sum()
        total_loss += loss.item()
        # dataloader_bar.set_postfix({'loss':loss.item()})
    
    # csv_list = pd.DataFrame(csv_list,columns=['predict','label'])
    # csv_list.to_csv('mask_result.csv')

    avg_loss = total_loss / len(val_loader)
    wandb.log({"validation avg loss":avg_loss})
    dataloader_bar.set_postfix({"validation avg loss": avg_loss})
    return avg_loss


if __name__=='__main__':
    args = parse_args()

    if args.eval:
        eval(args)
    elif args.infer:
        if os.path.isdir(args.img_path):
            print('path is directory')
            infer_from_dir(args)
        else:
            output = infer(args)
            print(output)
    else:
        wandb.init(project='Tire_Regression', name=args.name)
        wandb.config={
            "learning_rate":args.lr,
            "epochs": args.epochs,
            "batch_size": args.batch_size
        }
        train(args)
