# CSE111 – Data Structures

An independent Java 17 module demonstrating manual structures and sorting algorithms on PowerBank-style records. It does not replace the Spring backend database.

## Structure and classes

- `bst/`: `TreeNode`, `BinarySearchTree`, `StationBST`, and `PowerBankBST`; duplicate values are ignored, traversals and min/max are available.
- `queue/`: linked-node `CustomQueue`, `QueueNode`, and the `ReservationQueue` adapter; FIFO operations and `clear()` are implemented.
- `linkedlist/`: `ListNode`, `CustomLinkedList`, and `RentalHistoryList`; indexed insertion/removal and searches are implemented.
- `sorting/`: independent Bubble, Selection, Insertion, Merge, and Quick sort implementations.
- `Main.java`: runnable demonstration.
- `src/test/`: JUnit edge-case coverage.

## Run

From this directory:

```powershell
mvn test
mvn package
java -jar target\cse111-data-structures-1.0.0.jar
```

The structures are manually implemented; Java collections are used only for returned traversal/display results and tests.
