import nutchpy
import json

node_path = "nutch/runtime/local/crawldir/crawldb/current/part-00000/data"
seq_reader = nutchpy.SequenceReader()
results = seq_reader.read(node_path)

mimetype = {}


for domain in results:
	attributes = domain[1]
	if 'db_unfetched' in attributes:
		continue
	else:
		record = attributes.split("\n")
		for r in record:
			if 'Content-Type' in r:
				temp = r.split("=")
				mimetype[temp[1]] = 1

for k in mimetype.keys():
	print(k)
				













