import torch
from models.Efficient import Efficientformer
from PIL import Image
from torchvision import transforms

#model load
pre_trained = torch.load('60.pt')
model = Efficientformer(3,1).cuda()
model.state_dict = pre_trained.state_dict()
model.eval()


transforms = transforms.Compose([transforms.Resize((640,480)),
                            transforms.ToTensor()])


def main(input):
    """_summary_

    infernece tire depth by image

    Args:
        input (Image): PIL.Image
    """
    input = transforms(input)
    input = input.unsqueeze(0)
    img = input
    img = img.cuda()
    out = model(img)
    out= out.to('cpu')

if __name__== '__main__':
    
    input = Image.open('sample.jpg')
    main(input)
    


