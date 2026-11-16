package edu.powerbank.bst;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class BinarySearchTree<T extends Comparable<T>> {
    private TreeNode<T> root;

    public void insert(T value) {
        requireValue(value);
        root = insert(root, value);
    }

    public T search(T value) {
        requireValue(value);
        TreeNode<T> current = root;
        while (current != null) {
            int comparison = value.compareTo(current.value);
            if (comparison == 0) {
                return current.value;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return null;
    }

    public boolean contains(T value) {
        return search(value) != null;
    }

    public void delete(T value) {
        requireValue(value);
        root = delete(root, value);
    }

    public T findMin() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        TreeNode<T> current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.value;
    }

    public T findMax() {
        if (root == null) {
            throw new NoSuchElementException("Tree is empty");
        }
        TreeNode<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.value;
    }

    public List<T> inorderTraversal() {
        List<T> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    public List<T> preorderTraversal() {
        List<T> result = new ArrayList<>();
        preorder(root, result);
        return result;
    }

    public List<T> postorderTraversal() {
        List<T> result = new ArrayList<>();
        postorder(root, result);
        return result;
    }

    public List<T> inorder() {
        return inorderTraversal();
    }

    public int height() {
        return height(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    private TreeNode<T> insert(TreeNode<T> node, T value) {
        if (node == null) {
            return new TreeNode<>(value);
        }
        int comparison = value.compareTo(node.value);
        if (comparison < 0) {
            node.left = insert(node.left, value);
        } else if (comparison > 0) {
            node.right = insert(node.right, value);
        }
        return node;
    }

    private TreeNode<T> delete(TreeNode<T> node, T value) {
        if (node == null) {
            return null;
        }
        int comparison = value.compareTo(node.value);
        if (comparison < 0) {
            node.left = delete(node.left, value);
        } else if (comparison > 0) {
            node.right = delete(node.right, value);
        } else if (node.left == null) {
            return node.right;
        } else if (node.right == null) {
            return node.left;
        } else {
            TreeNode<T> successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.value = successor.value;
            node.right = delete(node.right, successor.value);
        }
        return node;
    }

    private void inorder(TreeNode<T> node, List<T> result) {
        if (node != null) {
            inorder(node.left, result);
            result.add(node.value);
            inorder(node.right, result);
        }
    }

    private void preorder(TreeNode<T> node, List<T> result) {
        if (node != null) {
            result.add(node.value);
            preorder(node.left, result);
            preorder(node.right, result);
        }
    }

    private void postorder(TreeNode<T> node, List<T> result) {
        if (node != null) {
            postorder(node.left, result);
            postorder(node.right, result);
            result.add(node.value);
        }
    }

    private int height(TreeNode<T> node) {
        return node == null ? 0 : 1 + Math.max(height(node.left), height(node.right));
    }

    private void requireValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Tree values cannot be null");
        }
    }
}
