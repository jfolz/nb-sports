import SBF, sys, os.path
import pylab as plt
from numpy import *

__usage__ = 'usage: python analyze.py location|acceleration PATH [PATH ...]'

def filter_location(loc, n=3, m=None):
	t,lat,long,alt,acc = SBF.get_all_attributes(loc)
	pos = array([lat,long])
	
	macc = 1 / acc[:n]
	mpos = (pos[:,:n] * macc).sum(1) / macc.sum()
	dpos = array([0,0])
	
	filtered = [mpos]
	alpha = .25
	for i in range(n+1,pos.shape[1]+1):
		macc = 1 / acc[i-n:i]
		mpos_new = (pos[:,i-n:i] * macc).sum(1) / macc.sum()
		dt = (t[i-1] - t[i-n:i].mean()) / 1000
		dpos = dpos * alpha + (mpos_new - mpos) * (1-alpha)
		filtered.append(mpos + dpos * dt)
		mpos = mpos_new
	
	filtered = array(filtered)
	return lat,long,filtered[:,0],filtered[:,1]

if len(sys.argv) < 3:
	print __usage__
	sys.exit(0)
	
command = sys.argv[1]

if not command in ["acceleration","location"]:
	print 'command',command,'not understood'
	print __usage__
	sys.exit(1)

files = []
for f in sys.argv[2:]:
	try: files.append(SBF.decode(f))
	except IOError as e: print(e)

if len(files) == 0: sys.exit(1)

if command == "acceleration":
	for f in files:
		d = SBF.get_all_attributes(f["acceleration"])
		plt.hold(1)
		t = range(len(d[0]))
		plt.plot(t,d[1])
		plt.plot(t,d[2])
		plt.plot(t,d[3])
		plt.show()

elif command == "location":
	plt.hold(1)
	colors = ['b','g','r','c']
	for i,f in enumerate(files):
		c = colors[i%len(colors)]
		lat,long,s_lat,s_long = filter_location(f["location"],n=5)
		plt.plot(lat,long,color=c,linewidth=5,alpha=.3)
		plt.plot(s_lat,s_long,color=c)
	plt.show()