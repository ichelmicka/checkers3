package com.example.model;
import java.util.ArrayDeque; 
import java.util.Deque; 

public class GroupFinder {
    private final Board board;
    
    public GroupFinder(Board board) {
        this.board = board;
    }

    public Group findGroup(int x, int y) {
        Stone color = board.get(x, y);
        if (color == Stone.EMPTY) {
            return null;
        }

        Group group = new Group(color);
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];

        Deque<Position> stack = new ArrayDeque<>();
        stack.push(new Position(x, y));

        int[][] dirs = {{1,0}, {-1, 0}, {0,1}, {0,-1}};
        
        while (!stack.isEmpty()) {
            Position p = stack.pop();
            int px = p.x;
            int py = p.y;

            if (visited[py][px]) {
                continue;
            }
            visited[py][px] = true;
            group.stones.add(p);

            for (int[] d : dirs) {
                int nx = px + d[0];
                int ny = py + d[1];

                if (!board.isOnBoard(nx, ny)) {
                    continue;
                }
                Stone s = board.get(nx, ny);
                if (s == Stone.EMPTY) {
                    group.liberties.add(new Position(nx, ny));
                }
                else if (s == color && !visited[ny][nx]) {
                    stack.push(new Position(nx, ny));
                }
            }

        }
        return group;
    }
}
