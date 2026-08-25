# Python / Algorithms / Data Structures / Tries

## 1. Trie Structure

A trie is a tree specialized for strings, where each node holds a dict of child nodes keyed by character -- words sharing a prefix share the same path through the tree. This makes it a natural fit for autocomplete.

```py
class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_word = False
```


## 2. Insert and Search

Insert walks the word character by character, creating a child node whenever one doesn't already exist for that character. Search follows the same path -- if any character is missing along the way, the word (or prefix) isn't in the trie.

```py
def insert(root, word):
    node = root
    for ch in word:
        node = node.children.setdefault(ch, TrieNode())
    node.is_word = True

def search(root, word):
    node = root
    for ch in word:
        if ch not in node.children:
            return None
        node = node.children[ch]
    return node
```


## 3. Autocomplete from a Prefix Node

Autocomplete finds the node for a given prefix, then recursively collects every word reachable below it -- the prefix lookup is the same `search` used above, only the endpoint changes from "return the node" to "collect everything under it."

```py
def collect_words(node, prefix, words):
    if node.is_word:
        words.append(prefix)
    for ch, child in node.children.items():
        collect_words(child, prefix + ch, words)
    return words

def autocomplete(root, prefix):
    node = search(root, prefix)
    return collect_words(node, prefix, []) if node else []
```
