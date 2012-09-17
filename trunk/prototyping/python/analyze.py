import SBF, sys, os.path
import pylab as plt

if len(sys.argv) < 2:
	print 'usage: python analyze.py rec_{timecode}'
	sys.exit(0)
elif not os.path.exists(sys.argv[1]):
	print 'file not found'
	sys.exit(1)

files = []
for f in sys.argv[1:]:
	files.append(SBF.decode(f))

tloc,lat,long,alt,acc = range(5)
tacc,x,y,z = range(4)

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