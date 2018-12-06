CC=gcc
FLAGS=-Wall -pedantic -I. -fdiagnostics-color
HEADERS= $(wildcard *.h)
TARGET=Bozons
OBJECTS= $(RECIPE:.c=.o)
RECIPE= $(wildcard *.c)

%.o: %.c 
	$(CC) -c $< $(FLAGS) -o $@

all: $(OBJECTS) $(HEADERS)
	$(CC) $(OBJECTS) -o $(TARGET) -lm

clean:
	rm -f *.o *~ $(TARGET)

run:
	make all
	./$(TARGET)
