import SBF, sys, os.path
import pylab as plt
from numpy import *

__usage__ = 'usage: python analyze.py location|acceleration PATH [PATH ...]'

def filter_location(loc, n=3, m=None):
	if not m: m = (n+1) / 2.
	t,lat,long,alt,acc = SBF.get_all_attributes(loc)
	pos = array([lat,long])
	
	deltapos = array([0,0])
	macc = acc[:n]
	mpos = (pos[:,:n] * macc).sum(1) / macc.sum()
	
	filtered = []
	filtered = [mpos]
	for i in range(n+1,pos.shape[1]):
		macc_new = acc[i-n:i]
		mpos_new = (pos[:,i-n:i] * macc_new).sum(1) / macc_new.sum()
		a = 1/macc.mean()
		b = 1/macc_new.mean()
		deltapos = deltapos * .75 * a + (mpos_new - mpos) * .25 * b
		deltapos /= a+b
		macc = macc_new
		deltat = (t[i-1] - t[i-2]) / 1000
		mpos = mpos + deltapos * deltat * m
		filtered.append(mpos)
	
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
		plt.plot(d[0],d[1])
		plt.plot(d[0],d[2])
		plt.plot(d[0],d[3])
		plt.show()

elif command == "location":
	plt.hold(1)
	for f in files:
		lat,long,s_lat,s_long = filter_location(f["location"],n=1)
		#plt.plot(lat,long)
		plt.plot(s_lat,s_long)
	plt.show()