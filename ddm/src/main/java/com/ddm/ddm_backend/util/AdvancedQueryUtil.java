package com.ddm.ddm_backend.util;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class AdvancedQueryUtil {
    private static final Set<String> OPERATORS = Set.of("AND", "OR", "NOT");
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "NOT", 3,
            "AND", 2,
            "OR", 1
    );

    private static boolean isOperator(String token) {
        return OPERATORS.contains(token.toUpperCase());
    }

    private static boolean hasHigherPrecedence(String opFromStack, String currentOp) {
        int stackPrecedence = PRECEDENCE.getOrDefault(opFromStack.toUpperCase(), -1);
        int currentPrecedence = PRECEDENCE.getOrDefault(currentOp.toUpperCase(), -1);
        return stackPrecedence >= currentPrecedence;
    }

    private Query toQuery(String operand) {
        String[] parts = operand.split(":", 2);
        String field = parts[0];
        String value = parts[1];
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return MatchPhraseQuery.of(m -> m
                    .field(field)
                    .query(value)
            )._toQuery();
        } else {
            return MatchQuery.of(m -> m
                    .field(field)
                    .query(value)
            )._toQuery();
        }
    }

    public List<String> toPostfix(List<String> infixTokens) {
        Stack<String> stack = new Stack<>();
        List<String> output = new ArrayList<>();

        for (String token : infixTokens) {
            if (isOperator(token)) {
                while (!stack.isEmpty() && isOperator(stack.peek()) &&
                        hasHigherPrecedence(stack.peek(), token)) {
                    output.add(stack.pop());
                }
                stack.push(token);
            } else {
                output.add(token);
            }
        }

        while (!stack.isEmpty()) {
            output.add(stack.pop());
        }

        return output;
    }

    public Query buildQuery(List<String> postfixTokens) {
        Stack<Query> stack = new Stack<>();

        for (String token : postfixTokens) {
            switch (token.toUpperCase()) {
                case "NOT" -> {
                    Query operand = stack.pop();
                    Query notQuery = BoolQuery.of(b -> b
                            .mustNot(operand)
                    )._toQuery();
                    stack.push(notQuery);
                }
                case "AND" -> {
                    Query right = stack.pop();
                    Query left = stack.pop();
                    Query andQuery = BoolQuery.of(b -> b
                            .must(left, right)
                    )._toQuery();
                    stack.push(andQuery);
                }
                case "OR" -> {
                    Query right = stack.pop();
                    Query left = stack.pop();
                    Query orQuery = BoolQuery.of(b -> b
                            .should(left, right)
                            .minimumShouldMatch("1")
                    )._toQuery();
                    stack.push(orQuery);
                }
                default -> {
                    stack.push(toQuery(token));
                }
            }
        }

        return stack.pop();
    }
}
