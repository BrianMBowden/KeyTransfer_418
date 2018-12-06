JC=javac
JFLAGS=-g
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES= \
	Server.java \
	Client.java \
	ServerThread.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
