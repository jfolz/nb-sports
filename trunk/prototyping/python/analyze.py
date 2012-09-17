import SBF, sys, os.path
import pylab as plt

if len(sys.argv) < 2:
	print 'usage: python analyze.py rec_{timecode}'
	sys.exit(0)
elif not os.path.exists(sys.argv[1]):
	print 'file not found'
	sys.exit(1)

data = SBF.decode(sys.argv[1])

for series in data["series"]:
	if series["identifier"] == 1: acc = series["data"]
	elif series["identifier"] == 2: loc = series["data"]

tacc = [e[0] for e in acc]
x = [e[1] for e in acc]
y = [e[2] for e in acc]
z = [e[3] for e in acc]

tloc = [e[0] for e in loc]
lat = [e[1] for e in loc]
long = [e[2] for e in loc]
alt = [e[3] for e in loc]
acc = [e[4] for e in loc]

plt.hold(1)
plt.plot(lat,long)
plt.show()

plt.hold(1)
plt.plot(tacc,x)
plt.plot(tacc,y)
plt.plot(tacc,z)
plt.show()