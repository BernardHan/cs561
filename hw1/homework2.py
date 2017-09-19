#!/usr/bin/python
import sys

class Lizard:
    def __init__(self):
        self.row = -1
        self.col = -1

    def move(self, row, col):
        self.row = row
        self.col = col

class NurseCell:
    def __init__(self, row, col, standOn=0):
        self.row = row
        self.col = col
        self.standOn = standOn

    def canAccess(self):
        return False if self.standOn == 2 else True

    def canPlace(self):
        return True if self.standOn == 0 else False

class NurseRoom:
    def __init__(self, size, numLizard):
        self.size = size
        self.numLizard = numLizard
        self.lizards = []
        self.trees = []

        for _ in range(0, numLizard):
            self.placeLizard(Lizard())

    def placeLizard(self, lizard):
        self.lizards.append(lizard)

    def placeTree(self, cell):
        self.trees.append(cell)


def buildRoom(size, lizard):
    # no trees for now
    room = NurseRoom(size, lizard)

    return room

def solve(method, room):
    # DFS first
    # use BFS/DFS place each lizard, try each comb, store danger cells into list in the process
    # if space not enough, just detect if a newly placed lizard will be eaten by iterate through all previous lizards
    cols = []
    results = []
    placeLizard(room, 0, cols, results, room.numLizard)
    return results

def placeLizard(room, row, cols, results, lizardLeft):
    if lizardLeft == 0:
        results.append(cols)
    elif row == room.size:
        return
    else:
        for col in range(0, room.size):
            if cantEat(row, col, cols):
                # this (row, col) won't let this lizard eat others
                if len(cols) <= row:
                    cols.append(col) # place this lizard
                else:
                    cols[row] = col

                # in this case, continue placing other lizards
                placeLizard(room, row + 1, cols, results, lizardLeft - 1)

    return

def cantEat(row, col, cols):
    # check the all previous lizards and current lizard to see if anyone can eat
    for checkRow in range(0, row):
        checkCol = cols[checkRow]

        if checkCol == col:
            # new lizard cannot be in the same column with others
            return False

        # check diagonals
        colDistance = abs(checkCol - col)
        rowDistance = abs(row - checkRow)
        # in a square, if between two nodes, if diff of col and row are the same
        # then it's a diagonal
        if colDistance == rowDistance:
            return False

    return True

def printResult(room, results):
    for result in results:
        for col in result:
            print('0' * col + '1' + '0' * (room.size - col - 1))
        print('\n' * 3)

if __name__ == '__main__':
    with open("input.txt", "r") as file:
        lines = file.readlines()
        lines = [line.strip() for line in lines]
        params = lines[:3]
        diagram = lines[3:]

        method = params[0]
        size = int(params[1])
        lizard = int(params[2])
    nurseRoom = buildRoom(size, lizard)
    results = solve(method, nurseRoom)

    printResult(nurseRoom, results[0:1])
    print(len(results))
