from numpy import *
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from MLP import MLP
from SBF import decode, get_all_attributes
from itertools import izip
import os
import pylab as plt

dir_train = 'data/train'
dir_test = 'data/test'

def get_labeled_files(d):
	l = os.listdir(d)
	l = filter(lambda f: not (f.endswith('.label') or f.startswith('.')), l)
	l = map(lambda f: d + os.sep + f, l)
	l = filter(lambda f: os.path.exists(f+'.label'), l)
	return l

def load_regions(f):
	data = open(f+'.label').read()
	regions = data.split('\n')
	regions = [r.split(' ') for r in regions]
	regions = [map(int,r) for r in regions]
	return regions

def generate_labels(f, n, w):
	regions = load_regions(f)
	labels = zeros(n)
	for a,b,l in regions:
		if b == 0: labels[a:] = l
		else: labels[a:b] = l
	
	out = zeros([n-w],dtype='int')
	for i in range(n-w):
		out[i] = round(labels[i:i+w].mean())
	return out

def generate_samples(data, w):
	i = 0
	n = data.shape[0]
	k = 1
	if len(data.shape) > 1: k = data.shape[1]
	out = zeros([n-w,k*w])
	for i in range(len(data)-w):
		out[i,:] = data[i:i+w].flat
	return out

def load_data(d, w):
	files = get_labeled_files(d)
	data = map(decode, files)
	data = [get_all_attributes(d['acceleration'])[1:].T for d in data]
	data = [d / 20 for d in data]
	s = concatenate([generate_samples(d,w) for d,f in izip(data,files)])
	l = concatenate([generate_labels(f,len(d),w) for d,f in izip(data,files)])
	return s,l

train,train_labels = load_data(dir_train,5)
test,test_labels = load_data(dir_test,5)

print train.shape,train_labels.shape,test.shape,test_labels.shape

#mlp = MLP(5,100,2)
#mlp.train(train,train_labels,epochs=5)
#result = mlp.classify(test)
classifier = RandomForestClassifier()
classifier.fit(train,train_labels)
result = classifier.predict(test)
print result.sum()
print sum(result == test_labels) / float(len(test_labels))
Ns = range(len(test_labels))
plt.plot(Ns,test[:,0])
plt.plot(Ns,result)
plt.plot(Ns,test_labels)
plt.show()