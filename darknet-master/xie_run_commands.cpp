#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>
using namespace std;

int main ()
{
	printf ("Checking if processor is available...");
	if (system(NULL)) puts ("Ok");
	else exit (EXIT_FAILURE);


	string commands = "";
	string base = "./darknet detector test data/obj.data cfg/yolo-obj.cfg backup/yolo-obj_3000.weights ";

	string line;
	vector<string> img_list;
	ifstream infile("test.txt");
	while (getline(infile, line))
	{
	    img_list.push_back(line);
	}

	//string image_path = "/Users/xiezhou/Documents/research_project/99photos/image";

	string mv_base = "mv predictions.png predictions/predictions";

	for(int i = 0; i < img_list.size(); i++){
		string command_test = base + img_list[i];
		string command_mv = mv_base + to_string(i) + ".png";
		string temp = command_test + " && " + command_mv + " && ";
		//cout << temp << endl;
		//cin >> input;
		commands += temp;
	}

	commands.pop_back();
	commands.pop_back();
	commands.pop_back();


	system(commands.c_str());
	return 0;
}