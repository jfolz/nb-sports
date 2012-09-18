import SBF, sys, os.path
import pylab as plt
from numpy import *

if len(sys.argv) < 2:
	print 'usage: python analyze.py rec_{timecode}'
	sys.exit(0)

files = []
for f in sys.argv[1:]:
	try: files.append(SBF.decode(f))
	except IOError as e: print(e)

if len(files) == 0: sys.exit(1)


tloc,lat,long,alt,acc = range(5)
tacc,x,y,z = range(4)

if len(files) > 1:
	plt.hold(1)
	for f in files:
		d = SBF.get_all_attributes(f["location"])
		plt.plot(d[lat],d[long])
	plt.show()
	
	for f in files:
		d = SBF.get_all_attributes(f["acceleration"])
		plt.hold(1)
		plt.plot(d[tacc],d[x])
		plt.plot(d[tacc],d[y])
		plt.plot(d[tacc],d[z])
		plt.show()
	
else:
	loc = files[0]["location"]
	t,lat,long,alt,acc = SBF.get_all_attributes(loc)
	pos = array([lat,long])
	filtered = []
	n = 3
	m = n / 2.
	deltapos = array([0,0])
	macc = acc[:n]
	mpos = (pos[:,:n] * macc).sum(1) / macc.sum()
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
	plt.hold(1)
	plt.plot(lat,long)
	plt.plot(filtered[:,0],filtered[:,1])
	plt.show()