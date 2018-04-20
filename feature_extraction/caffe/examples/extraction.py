# set up Python environment: numpy for numerical routines, and matplotlib for plotting
import numpy as np
import os
import matplotlib.pyplot as plt
from os import listdir

# set display defaults
## plt.rcParams['figure.figsize'] = (10, 10)        # large images
## plt.rcParams['image.interpolation'] = 'nearest'  # don't interpolate: show square pixels
## plt.rcParams['image.cmap'] = 'gray'  # use grayscale output rather than a (potentially misleading) color heatmap

import sys
caffe_root = '../'  # this file should be run from {caffe_root}/examples (otherwise change this line)
sys.path.insert(0, caffe_root + 'python')

import caffe
# If you get "No module named _caffe", either you have not built pycaffe or you have the wrong path.

import os
if os.path.isfile(caffe_root + 'models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel'):
    print 'CaffeNet found.'
else:
    print 'Downloading pre-trained CaffeNet model...'
    
caffe.set_mode_cpu()

model_def = caffe_root + 'models/bvlc_reference_caffenet/deploy.prototxt'
model_weights = caffe_root + 'models/bvlc_reference_caffenet/bvlc_reference_caffenet.caffemodel'

net = caffe.Net(model_def,      # defines the structure of the model
                model_weights,  # contains the trained weights
                caffe.TEST)     # use test mode (e.g., don't perform dropout)

# load the mean ImageNet image (as distributed with Caffe) for subtraction
mu = np.load(caffe_root + 'python/caffe/imagenet/ilsvrc_2012_mean.npy')
mu = mu.mean(1).mean(1)  # average over pixels to obtain the mean (BGR) pixel values
print 'mean-subtracted values:', zip('BGR', mu)

# create transformer for the input called 'data'
transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})

transformer.set_transpose('data', (2,0,1))  # move image channels to outermost dimension
transformer.set_mean('data', mu)            # subtract the dataset-mean value in each channel
transformer.set_raw_scale('data', 255)      # rescale from [0, 1] to [0, 255]
transformer.set_channel_swap('data', (2,1,0))  # swap channels from RGB to BGR


# set the size of the input (we can skip this if we're happy
#  with the default; we can also change it later, e.g., for different batch sizes)
net.blobs['data'].reshape(50,        # batch size
                          3,         # 3-channel (BGR) images
                          227, 227)  # image size is 227x227

image_dir = 'examples/feature_extraction/photos19390114_cropped_contrast/' #directory for images
working_dir = '/Users/xiezhou/Documents/research_project/' #custom working directory
abs_dir = working_dir+'feature_extraction/caffe/'
feature_dir = 'examples/feature_extraction/photos19390114_contrast_features/'
os.mkdir(abs_dir+feature_dir)

imagesList = listdir(caffe_root+image_dir)
for img in imagesList:
	if img == '.DS_Store':
		continue
	image = caffe.io.load_image(caffe_root + image_dir + img)
	transformed_image = transformer.preprocess('data', image)
	## plt.imshow(image)

	# copy the image data into the memory allocated for the net
	net.blobs['data'].data[...] = transformed_image

	### perform classification
	output = net.forward()

	#output_prob = output['prob'][0]  # the output probability vector for the first image in the batch

	#print 'predicted class is:', output_prob.argmax()

	feature = net.blobs['pool5'].data[0].flatten()

	#print 'fature size after flattening is: ' , feature.flatten().shape

	with open(caffe_root + feature_dir + os.path.splitext(img)[0], 'w') as f:
			f.writelines(','.join(str(num) for num in feature))