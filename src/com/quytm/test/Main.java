package com.quytm.test;

import java.util.Map;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Stack;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String OPERATORS = "+-*/";

    static class CircularDependencyException extends Exception {
        private String message;

        CircularDependencyException(String message) {
            this.message = message;
        }
    }

    private static class Cell {
        String name;
        String[] raw;
        double value;
        boolean isCalculated;

        Cell(String name, String raw) {
            this.name = name;
            if (isNumber(raw)) {
                this.raw = new String[]{raw};
                this.value = Double.parseDouble(raw);
                isCalculated = true;
            } else {
                this.raw = raw.split(" ");
            }
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell) {
                return this.name.equals(((Cell) obj).name);
            }
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        Map<String, Cell> cellMap = getStdin();

        try {
            process(cellMap);
            printStdout(cellMap);
        } catch (CircularDependencyException e) {
            System.out.println(e.message);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static Map<String, Cell> getStdin() {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        in.nextLine();  // next line
        SortedMap<String, Cell> cellSet = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            String cellName = in.nextLine();
            String cellValue = in.nextLine();
            Cell cell = new Cell(cellName, cellValue);
            cellSet.put(cellName, cell);
        }
        return cellSet;
    }

    private static void process(Map<String, Cell> cells) throws CircularDependencyException {
        List<String> notCalCells = new LinkedList<>();
        // Add all cell which is not calculated in to List
        cells.forEach((key, value) -> {
            if (!value.isCalculated) {
                notCalCells.add(key);
            }
        });


        int loopSize = notCalCells.size();
        for (int i = 0; i < loopSize; i++) {
            if (notCalCells.isEmpty()) break;

            List<String> willRemoveCell = new ArrayList<>();

            notCalCells.forEach(key -> {
                Cell cell = cells.get(key);
                boolean replaceAllSuccess = replaceCellValue(cell.raw, cells);
                if (replaceAllSuccess) {
                    cell.value = calculator(cell.raw);
                    cell.isCalculated = true;
                    willRemoveCell.add(key);
                }
            });

            // remove all cell which is calculated
            notCalCells.removeAll(willRemoveCell);
        }

        // When cannot calculate all cell
        if (!notCalCells.isEmpty()) {
            throw new CircularDependencyException(buildErrorMessage(notCalCells));
        }
    }

    private static void printStdout(Map<String, Cell> cells) {
        cells.forEach((key, value) -> {
            System.out.println(value.name);
            System.out.println(value.value);
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static boolean isNumber(String number) {
        Pattern pattern = Pattern.compile("[+-]?\\d+\\.?(\\d+)?");  // check double-string value
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    private static boolean isOperator(String operator) {
        return OPERATORS.contains(operator);
    }

    // Replace Cell by Cell's value in formula
    private static boolean replaceCellValue(String[] raw, Map<String, Cell> cells) {
        for (int i = 0; i < raw.length; i++) {
            if (!isNumber(raw[i]) && !isOperator(raw[i])) {
                Cell cell = cells.get(raw[i]);
                if (cell == null || !cell.isCalculated) return false;

                raw[i] = String.valueOf(cell.value);
            }
        }
        return true;
    }

    private static String buildErrorMessage(List<String> notCalCells) {
        StringBuilder keys = new StringBuilder(notCalCells.get(0));
        for (int i = 1; i < notCalCells.size() - 1; i++) {
            keys.append(", ").append(notCalCells.get(i));
        }
        keys.append(" and ").append(notCalCells.get(notCalCells.size() - 1));

        return "Circular dependency between " + keys.toString() + " detected";
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static double calculator(String[] strArr) {
        Stack<Double> operands = new Stack<>();

        for (String str : strArr) {
            if (str.trim().equals("")) {
                continue;
            }

            switch (str) {
                case "+":
                case "-":
                case "*":
                case "/":
                    double right = operands.pop();
                    double left = operands.pop();
                    double value = 0;
                    switch (str) {
                        case "+":
                            value = left + right;
                            break;
                        case "-":
                            value = left - right;
                            break;
                        case "*":
                            value = left * right;
                            break;
                        case "/":
                            value = left / right;
                            break;
                        default:
                            break;
                    }
                    operands.push(value);
                    break;
                default:
                    operands.push(Double.parseDouble(str));
                    break;
            }
        }
        return operands.pop();
    }
}