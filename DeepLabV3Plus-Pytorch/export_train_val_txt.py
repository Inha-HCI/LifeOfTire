import os
from sklearn.model_selection import train_test_split

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

train_file_name, val_file_name = train_test_split(file_name, test_size = 0.05)

with open('./datasets/data/tire_dataset_voc/tire_train.txt', 'w') as f:
    f.write('\n'.join(train_file_name))

with open('./datasets/data/tire_dataset_voc/tire_val.txt', 'w') as f:
    f.write('\n'.join(val_file_name))