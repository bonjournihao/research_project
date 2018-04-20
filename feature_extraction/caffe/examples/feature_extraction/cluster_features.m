
%load features
image_name = 'photos19390114';
dir_name = strcat(image_name,'_features/');
filename_list = dir(dir_name);
%get rid of .././/.ds_o
filename_list(1:2) = [];
features = zeros(size(filename_list,1), 9216);

for i = 1:size(filename_list,1)
    filename = filename_list(i).name;
    features(i,:) = csvread(strcat(dir_name,filename));
end


% distances between pairs of items
feature_dist = pdist(features,'cosine');
square_dist = squareform(feature_dist);
dist = ones(size(square_dist,1),size(square_dist,2)) - square_dist;
N = size(features,1);
M=N*N-N;
j=1;
s=zeros(M,3);
for i=1:N
   for k=[1:i-1,i+1:N]
     s(j,1)=i;
     s(j,2)=k;
     s(j,3)=dist(i,k);
     j=j+1;
   end
end
p=0.6;

%{
p = zeros(size(dist,2),1);
for i = 1:size(p,1)
    line = dist(i,:);
    line(i) = [];
    line = sort(line,'descend');
    p(i,:) = mean(line(1:10));
end
%}

[idx,netsim,dpsim,expref]=apclusterSparse(s,p);

clusters = unique(idx);
counts =hist(idx,clusters)'; 

[C,I] = sort(counts,'descend');
cluster_items_sorted = clusters(I);
top_how_many = 10;
%%top_cluster = clusters(I(1));

rows = 3;
cols =ceil(top_how_many/3);

img_dir = strcat(image_name,'_cropped/');
for i=1:top_how_many
    cluster_index = cluster_items_sorted(i);
    img_name = strcat(filename_list(cluster_index).name,'.jpg');
    img = imread(strcat(img_dir,img_name));
    subplot(rows,cols,i); 
    imshow(img);
    str = ['cluster size: ',num2str(C(i))];
    title(str);
end

%{
% perform hierarchical clustering
hier_htree = linkage(feature_dist,'single');
% check consistancy: higher value corresponds to higher correlation
inconsis = cophenet(hier_htree,feature_dist);



% create clusters, set inconsist cutoff
clustered_features = cluster(hier_htree,'cutoff',0.9,'depth',5); 

num_clusters = max(clustered_features);
num_items = size(features,1);

% get cluster sizes + get symbolic items
% symbolic items
cluster_items = zeros(num_clusters,1); 
% item number in each cluster
cluster_counts = zeros(num_clusters,1);

for item=1:size(clustered_features)
    index = clustered_features(item);
    if cluster_items(index) == 0
        cluster_items(index) = item;
    end
    cluster_counts(index) = cluster_counts(index) + 1;
end


% get top 10 clusters
[C,I] = sort(cluster_counts,'descend');
% sort cluster symbols based on I
cluster_items_sorted = cluster_items(I);
top_how_many = 10;


rows = 3;
cols =ceil(top_how_many/3);

img_dir = strcat(image_name,'_cropped/');
for i=1:top_how_many
    cluster_index = cluster_items_sorted(i);
    img_name = strcat(filename_list(cluster_index).name,'.jpg');
    img = imread(strcat(img_dir,img_name));
    subplot(rows,cols,i); 
    imshow(img);
    str = ['cluster size: ',num2str(C(i))];
    title(str);
end
%}
