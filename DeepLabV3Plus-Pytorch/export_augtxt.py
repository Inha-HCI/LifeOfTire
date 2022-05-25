import os


dir_path = "./datasets/data/tire_dataset_voc/JPEGImages"


file_list = os.listdir(dir_path)
file_name = []
print(file_list)


for file in file_list:
    if file.count(".") == 1: 
        name = file.split('.')[0]
        file_name.append(name)
        
print(file_name)
print(len(file_name))


with open("./datasets/data/tire_aug.txt", "w") as f:
    f.write('\n'.join(file_name))