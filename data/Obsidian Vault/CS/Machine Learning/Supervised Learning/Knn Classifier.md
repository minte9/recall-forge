## 1. Predict unknown

We provide training dataset ponts (features) and label (target).
We train the model (with k=3 nearest neighbors constrain).
We are able to predict the label (y) for a new (unknown) data point.

```python
from sklearn.neighbors import KNeighborsClassifier

# Training dataset
# ----------------
X = [[0,0],
     [1,1],
     [2,2],
     [3,3]]
y = [0, 1, 0, 1]

# Train the model
# ---------------
knn = KNeighborsClassifier(n_neighbors=3)
knn.fit(X, y)

# Make predictions for unknown
# ----------------------------
x_unknown = [1,2]
y_pred = knn.predict([x_unknown])

# Output results
# --------------
print(x_unknown)  # [1, 2]
print(y_pred)     # [0]

```

## 2. Predict from dataset

We use pandas library to transform a dictonary dataset into a DataFrame.
Fruits dataset contains heights, widths and labels (fruit name).

```python
from sklearn.neighbors import KNeighborsClassifier
import pandas as pd
from icecream import ic

# Training dataset (dictionary)
# ----------------------------
data = {
  'height': [
    3.91, 7.09, 10.48, 9.21, 7.95, 7.62, 7.95, 4.69, 7.50, 7.11, 
    4.15, 7.29, 8.49, 7.44, 7.86, 3.93, 4.40, 5.5, 8.10, 8.69
  ], 
  'width': [
     5.76, 7.69, 7.32, 7.20, 5.90, 7.51, 5.32, 6.19, 5.99, 7.02, 
     5.60, 8.38, 6.52, 7.89, 7.60, 6.12, 5.90, 4.5, 6.15, 5.82
  ],
  'fruit': [
    'Mandarin', 'Apple', 'Lemon', 'Lemon', 'Lemon', 'Apple', 'Mandarin', 
    'Mandarin', 'Lemon', 'Apple', 'Mandarin', 'Apple', 'Lemon', 'Apple', 
    'Apple', 'Apple', 'Mandarin', 'Lemon', 'Lemon', 'Lemon'
  ]
} 

# Transform dataset
# -----------------
df = pd.DataFrame(data)
df = df.sort_values(by=['fruit', 'width', 'height'])
ic(df)

X = df[['height', 'width']].values
y = df.fruit.values
ic(X, y)

# Train the model
# ---------------
knn = KNeighborsClassifier(n_neighbors=3)
knn.fit(X, y)

# Make predictions
# ----------------
unknown_item = [9,3]
unknown_items = [[9, 3], [4, 5], [2, 5], [8, 9], [5, 7]]

prediction = knn.predict([unknown_item])
predictions = knn.predict(unknown_items)


# Output results
# --------------
ic(unknown_item, prediction)
ic(unknown_items,predictions)

"""
ic| df:     height  width     fruit
        15    3.93   6.12     Apple
        9     7.11   7.02     Apple
        5     7.62   7.51     Apple
        14    7.86   7.60     Apple
        1     7.09   7.69     Apple
        13    7.44   7.89     Apple
        11    7.29   8.38     Apple
        17    5.50   4.50     Lemon
        19    8.69   5.82     Lemon
        4     7.95   5.90     Lemon
        8     7.50   5.99     Lemon
        18    8.10   6.15     Lemon
        12    8.49   6.52     Lemon
        3     9.21   7.20     Lemon
        2    10.48   7.32     Lemon
        6     7.95   5.32  Mandarin
        10    4.15   5.60  Mandarin
        0     3.91   5.76  Mandarin
        16    4.40   5.90  Mandarin
        7     4.69   6.19  Mandarin
ic| X: array([[ 3.93,  6.12],
              [ 7.11,  7.02],
              [ 7.62,  7.51],
              [ 7.86,  7.6 ],
              [ 7.09,  7.69],
              [ 7.44,  7.89],
              [ 7.29,  8.38],
              [ 5.5 ,  4.5 ],
              [ 8.69,  5.82],
              [ 7.95,  5.9 ],
              [ 7.5 ,  5.99],
              [ 8.1 ,  6.15],
              [ 8.49,  6.52],
              [ 9.21,  7.2 ],
              [10.48,  7.32],
              [ 7.95,  5.32],
              [ 4.15,  5.6 ],
              [ 3.91,  5.76],
              [ 4.4 ,  5.9 ],
              [ 4.69,  6.19]])
    y: array(['Apple', 'Apple', 'Apple', 'Apple', 'Apple', 'Apple', 'Apple',
              'Lemon', 'Lemon', 'Lemon', 'Lemon', 'Lemon', 'Lemon', 'Lemon',
              'Lemon', 'Mandarin', 'Mandarin', 'Mandarin', 'Mandarin',
              'Mandarin'], dtype=object)
ic| unknown_item: [9, 3], prediction: array(['Lemon'], dtype=object)
ic| unknown_items: [[9, 3], [4, 5], [2, 5], [8, 9], [5, 7]]
    predictions: array(['Lemon', 'Mandarin', 'Mandarin', 'Apple', 'Mandarin'], dtype=object)
"""
```

## 3. Model Evaluation

The dataset is splitted in two datasets (training and test) that will be used to evaluate the model.
The score is the difference between actual and predicted labels.
A score of 1.0 means that the model correctly predicted all labels (100%).

```python
import pandas as pd
from sklearn.neighbors import KNeighborsClassifier
from sklearn import metrics

# Training and TEST datasets
# --------------------------
D1 = pd.DataFrame({
  'height': [
    3.91, 7.09, 10.48, 9.21, 7.95, 7.62, 7.95, 4.69, 7.50, 7.11, 
    4.15, 7.29, 8.49, 7.44, 7.86, 3.93, 4.40, 5.5, 8.10, 8.69
  ], 
  'width': [
     5.76, 7.69, 7.32, 7.20, 5.90, 7.51, 5.32, 6.19, 5.99, 7.02, 
     5.60, 8.38, 6.52, 7.89, 7.60, 6.12, 5.90, 4.5, 6.15, 5.82
  ],
  'fruit': [
    'Mandarin', 'Apple', 'Lemon', 'Lemon', 'Lemon', 'Apple', 'Mandarin', 
    'Mandarin', 'Lemon', 'Apple', 'Mandarin', 'Apple', 'Lemon', 'Apple', 
    'Apple', 'Apple', 'Mandarin', 'Lemon', 'Lemon', 'Lemon'
  ]
})

# Test dataset
D2 = pd.DataFrame({
    'height': [4, 4.47, 6.49, 7.51, 8.34],
    'width':  [6.5, 7.13, 7, 5.01, 4.23],
    'fruit':  ['Mandarin', 'Mandarin', 'Apple', 'Lemon', 'Lemon']
})


# Transform datasets
# ------------------
X1 = D1[['height', 'width']].values
y1 = D1.fruit.values

X2 = D2[['height', 'width']].values
y2 = D2.fruit.values

# Train the model
# ---------------
knn = KNeighborsClassifier(n_neighbors=3)
knn.fit(X1, y1)


# Evaluate the model
# ------------------
predictions1 = knn.predict(X1)
predictions2 = knn.predict(X2)

score1 = metrics.accuracy_score(y1, predictions1)
score2 = metrics.accuracy_score(y2, predictions2)

print("Model score on training dataset:", score1 * 100)
print("Model score on test dataset:", score2 * 100)

# -----------------------------------
# Model score on training dataset: 85.0
# Model score on test dataset: 100.0
```

## 4. Score Graph

By plotting the results graph we can see that the model perform optimally 
for k between 3 and 7 (for test set).

```python
import pandas as pd
from sklearn.neighbors import KNeighborsClassifier
from sklearn import metrics
import matplotlib.pyplot as plt

# Training and TEST datasets
# --------------------------
D1 = pd.DataFrame({
  'height': [
    3.91, 7.09, 10.48, 9.21, 7.95, 7.62, 7.95, 4.69, 7.50, 7.11, 
    4.15, 7.29, 8.49, 7.44, 7.86, 3.93, 4.40, 5.5, 8.10, 8.69
  ], 
  'width': [
     5.76, 7.69, 7.32, 7.20, 5.90, 7.51, 5.32, 6.19, 5.99, 7.02, 
     5.60, 8.38, 6.52, 7.89, 7.60, 6.12, 5.90, 4.5, 6.15, 5.82
  ],
  'fruit': [
    'Mandarin', 'Apple', 'Lemon', 'Lemon', 'Lemon', 'Apple', 'Mandarin', 
    'Mandarin', 'Lemon', 'Apple', 'Mandarin', 'Apple', 'Lemon', 'Apple', 
    'Apple', 'Apple', 'Mandarin', 'Lemon', 'Lemon', 'Lemon'
  ]
})

# Test dataset
D2 = pd.DataFrame({
    'height': [4, 4.47, 6.49, 7.51, 8.34],
    'width':  [6.5, 7.13, 7, 5.01, 4.23],
    'fruit':  ['Mandarin', 'Mandarin', 'Apple', 'Lemon', 'Lemon']
})


# Transform datasets
# ------------------
X1 = D1[['height', 'width']].values
y1 = D1.fruit.values

X2 = D2[['height', 'width']].values
y2 = D2.fruit.values


# Evaluate the score for different params
# ---------------------------------------
k = []
score1 = []
score2 = []

for i in range(len(X1)):
    _k = i + 1  # different k neighbors

    knn = KNeighborsClassifier(n_neighbors=_k)
    knn.fit(X1, y1)

    _score1 = metrics.accuracy_score(y1, knn.predict(X1))
    _score2 = metrics.accuracy_score(y2, knn.predict(X2))  # test dataset

    k.append(_k)
    score1.append(_score1 * 100)
    score2.append(_score2 * 100)

    # Output accuracy for each _k (n_neighbors)
    print(f"k={_k} | score1: {score1[i]} | score2: {score2[i]}")

    """
    k=1 | score1: 100.0 | score2: 40.0
    k=2 | score1: 95.0 | score2: 60.0
    k=3 | score1: 85.0 | score2: 100.0
    k=4 | score1: 85.0 | score2: 100.0
    k=5 | score1: 85.0 | score2: 100.0
    k=6 | score1: 85.0 | score2: 100.0
    k=7 | score1: 85.0 | score2: 100.0
    k=8 | score1: 85.0 | score2: 100.0
    k=9 | score1: 85.0 | score2: 80.0
    k=10 | score1: 85.0 | score2: 60.0
    k=11 | score1: 80.0 | score2: 60.0
    k=12 | score1: 90.0 | score2: 60.0
    k=13 | score1: 65.0 | score2: 60.0
    k=14 | score1: 55.00000000000001 | score2: 60.0
    k=15 | score1: 55.00000000000001 | score2: 60.0
    k=16 | score1: 45.0 | score2: 60.0
    k=17 | score1: 50.0 | score2: 60.0
    k=18 | score1: 50.0 | score2: 60.0
    k=19 | score1: 40.0 | score2: 40.0
    k=20 | score1: 40.0 | score2: 40.0
    """


# Plot results
# ------------

# Plot train score
plt.scatter(k, score1)  # function
plt.plot(k, score1, '-', label='train')  # data points

# Plot test score
plt.scatter(k, score2)  # function
plt.plot(k, score2, '-', label='test')  # data points

# Plot configurations
plt.axis([max(k),min(k)+1, 0, 100])
plt.xlabel('number of nearest neighbours (k)', size = 13)
plt.ylabel('accuracy score', size = 13)
plt.title('Model Performance vs Complexity', size = 20)
plt.legend()

# Output
plt.show()
```

![[knn image 1.png]]
## 5. Decision Boundaries

Decision boundaries of KNN on a graph (optimal fit for k=5).

![[knn image 2.png]]

![[knn image 3.png]]

![[knn image 4.png]]

