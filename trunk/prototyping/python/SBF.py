import struct, json

def s(fmt): return struct.Struct(fmt)

fmt_identifier = s('>b')



def _get_header(data):
	s = ''
	i = 0
	c = data[i]
	while c != '\n':
		s += c
		i += 1
		c = data[i]
	return data[i+1:],s



def _parse_header(h):
	start = 'stupid binary format'
	if h[:len(start)] != start:
		raise AttributeException("\"" + h + "\" is not a valid header")
	d = json.loads(h[len(start):])
	if d["version"] == 2:
		formats = []
		for series in d["series"]:
			f = '>'
			for t in series["types"]:
				if t == "byte": f +='b'
				elif t == "short": f+='h'
				elif t == "int": f+='i'
				elif t == "long": f+='q'
				elif t == "float": f+='f'
				elif t == "double": f+='d'
			series["unpack_format"] = s(f)
	else:
		raise AttributeException("stupid binary format version " + d["version"] + " not supported")
	
	return d


def decode(f):
	if type(f) is file:
		data = f.read()
		f.close()
	elif type(f) is str:
		try: data = open(f).read()
		except: data = f
	else:
		data = f
	
	data,header = _get_header(data)
	header = _parse_header(header)
	
	formats = {}
	lists = {}
	for series in header["series"]:
		lists[series["identifier"]] = []
		formats[series["identifier"]] = series["unpack_format"]
	
	while len(data) > 0:
		identifier = fmt_identifier.unpack(data[:1])[0]
		data = data[1:]
		f = formats[identifier]
		entry = f.unpack(data[:f.size])
		if entry: lists[identifier].append(entry)
		else: break
		data = data[f.size:]
	
	for series in header["series"]:
		series["data"] = lists[series["identifier"]]
	
	return header