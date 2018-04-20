Image detection using darknet framework  -> cd darknet-master/
1)	Create a file text.txt that lists testing image paths, one image per line.
2)	C++, Run image detection and store visual detection results into predictions/
.	/xie_out    (compile xie_run_commands.cpp) (this step can be skipped)
3)	Bash, Run validation to generate detection bounding boxes in each image, store coordinates into results/comp4_det_test_pictaure.txt 
.	/darknet detector valid data/obj.data cfg/yolo-obj.cfg backup/yolo-obj_3000.weights
4)	Java crop_image, 
change line 19 to project directory; line 20 to testing photo directory (or change the photo directory accordingly)
ex. test_smaller folder will generate test_smaller_coord and test_smaller_cropped
5)	In exrtaction/caffe/examples/feature_extraction/cluster_features.m,
change the 3rd line: image_name to the cropped image directory (ex.test_smaller_cropped) to direct the later codes for image matching
also in exrtaction/caffe/examples/extraction.py, change line 55 to working directory,
54 to image_input directory, 57 to feature output directory
 Feature extraction using caffe framework  -> cd feature_exrtaction/
1)	Python: feature extraction on images (run caffe/examples/extraction.py)
2)	Matlab: Use ap clustering for features, for image matching 
run caffe/examples/feature_extraction/cluster_features.m
the cluster with the most features would be shown in its image form.
