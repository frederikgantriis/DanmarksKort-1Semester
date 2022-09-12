package bfst22.vector.model.KdTree;

import java.io.Serializable;

public class Point implements Serializable {
    public static final long serialVersionUID = 9088642;
    private float value;
    Point leftChild, rightChild;

    public Point(float value){
        this.value = value;
    }

    public Point(float value, Point leftChild, Point rightChild){
        this.value = value;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public void setRightChild(Point rightChild) {
        this.rightChild = rightChild;
    }

    public void setLeftChild(Point leftChild) {
        this.leftChild = leftChild;
    }

    public Point getRightChild(){
        return rightChild;
    }

    public Point getLeftChild(){
        return leftChild;
    }

    public float getValue() {
        return value;
    }
}
