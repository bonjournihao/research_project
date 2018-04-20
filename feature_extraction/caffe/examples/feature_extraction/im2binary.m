root_dir = 'photos19390107_cropped';
root = strcat(root_dir,'/');

new_dir = strcat(root_dir,'_contrast/');
mkdir(new_dir);

MyFolderInfo = dir(root);
MyFolderInfo(1:3,:) = [];
for i=1:size(MyFolderInfo,1)
    filename = strcat(root,MyFolderInfo(i).name);
    %%display(filename);
    I = imread(filename);
    I = imadjust(I,stretchlim(I));
    imwrite(I,strcat(new_dir,MyFolderInfo(i).name));
end
%%I = imread('photos19390114_cropped/Paris-soir-19390114-pictaure1.jpg');
%{
imshow(I);
I = imadjust(I,stretchlim(I));
figure;
imshow(I);
%}
