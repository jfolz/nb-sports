from numpy import dot, eye, outer, exp, zeros, sqrt
from numpy.random import rand
from itertools import izip

def sigmoid(x): return 1. / (1. + exp(-x))

class MLP:
	def __init__(self,n_in,n_hidden,n_out):
		self.Cs = eye(n_out)
		self.A = rand(n_in,n_hidden) - .5
		self.a = rand(n_hidden) - .5
		self.B = rand(n_hidden,n_out) - .5
		self.b = rand(n_out) - .5
	
	def classify(self,x,raw=False):		y = sigmoid(dot(x,self.A)+self.a)
		z = sigmoid(dot(y,self.B)+self.b)
		if raw: return z
		else: return z.argmax(-1)
	
	def train(self,X,C,eta=.15,epochs=1,eps=0.01):
		for epoch in range(epochs):
			run = False
			for x,c in izip(X,C):				y = sigmoid(dot(x,self.A)+self.a)
				z = sigmoid(dot(y,self.B)+self.b)
				d = z - self.Cs[c]
				if (d**2).mean() < eps: continue
				else: run = True
				dz = 2 * (d) * z * (1 - z)
				dy = dot(dz,self.B.T) * (y*(1-y))
				self.B -= eta * outer(y,dz)
				self.b -= eta * dz
				self.A -= eta * outer(x,dy)
				self.a -= eta * dy
			if not run: break