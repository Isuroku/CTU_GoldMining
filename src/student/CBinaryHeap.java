package student;


public class CBinaryHeap
{
    private static final int DEFAULT_CAPACITY = 100;

    private int _current_size;      // Number of elements in heap
    private Comparable[] _array; // The heap array

    public CBinaryHeap()
    {
        _current_size = 0;
        _array = new Comparable[DEFAULT_CAPACITY+1];
    }

    public CBinaryHeap(Comparable[] items) {
        _current_size = items.length;
        _array = new Comparable[items.length+1];

        System.arraycopy(items, 0, _array, 1, items.length);
        BuildHeap();
    }

    public void Insert(Comparable x)
    {
        if (_current_size + 1 == _array.length) {
            double_array( );
        }

        // Percolate up
        int hole = ++_current_size;
        _array[0] = x;

        for (; x.compareTo(_array[hole/2]) < 0; hole /= 2) {
            _array[hole] = _array[hole/2];
        }
        _array[hole] = x;
    }

    private Comparable FindMin()
    {
        if (IsEmpty()) {
            throw new RuntimeException( "Empty binary heap" );
        }
        return _array[1];
    }

    public Comparable DeleteMin()
    {
        Comparable minItem = FindMin();
        _array[1] = _array[_current_size--];
        percolate_down(1);

        return minItem;
    }

    private void BuildHeap()
    {
        for(int i = _current_size /2; i > 0; i--) {
            percolate_down(i);
        }
    }

    public boolean IsEmpty()
    {
        return _current_size == 0;
    }

    public int Size()
    {
        return _current_size;
    }

    public void MakeEmpty()
    {
        _current_size = 0;
    }

    private void percolate_down(int hole)
    {
        int child;
        Comparable tmp = _array[ hole ];

        for(; hole * 2 <= _current_size; hole = child ) {
            child = hole * 2;
            if (child != _current_size && _array[child+1].compareTo(_array[child])  < 0) {
                child++;
            }
            if (_array[child].compareTo(tmp) < 0) {
                _array[hole] = _array[child];
            } else {
                break;
            }
        }
        _array[hole] = tmp;
    }

    private void double_array()
    {
        Comparable [ ] newArray;

        newArray = new Comparable[ _array.length * 2 ];
        System.arraycopy(_array, 0, newArray, 0, _array.length);
        _array = newArray;
    }
}
