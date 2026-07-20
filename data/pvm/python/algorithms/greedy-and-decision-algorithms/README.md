## PVM / Python / Algorithms / Greedy & Decision Algorithms

### 1. Coin Change (Greedy)

The greedy strategy for making change is simple: always take as many of the largest coin as possible before moving to the next smaller one. It's fast and easy to reason about, but doesn't guarantee the true minimum for every coin system -- it works here because the denominations are "canonical" (each divides evenly enough into the next).

~~~py
D = [1, 5, 10, 25]
R = []

target = 63

for n in reversed(D):
    while n <= target - sum(R):
        R.append(n)

print(R, f"Minimum number of coins = {len(R)}")

"""
    [25, 25, 10, 1, 1, 1] Minimum number of coins = 6
"""
~~~


### 2. Minimax (Decision-Making)

Minimax evaluates a game tree where two players alternate turns: the Max player picks the branch with the highest score, the Min player picks the branch with the lowest, each assuming the other plays optimally against them. The result is the best score Max can guarantee no matter what Min does in response.

~~~py
def minimax(node, player):
    if isinstance(node, int):  # base case -- a leaf score
        return node

    lv = minimax(node[0], not player)
    rv = minimax(node[1], not player)

    return max(lv, rv) if player else min(lv, rv)

tree = [[9, 5], [-3, -2]]

print('Max to play / Best score =', minimax(tree, True))
print('Min to play / Best score =', minimax(tree, False))

"""
    Max to play / Best score = 5
    Min to play / Best score = -2
"""
~~~
