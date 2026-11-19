# Autómata de la bomba · Grupo 04

> Archivo generado por la aplicación a partir de config/integrantes.csv. No lo edite a mano.

| Integrante | Cédula | Último dígito | Coordenada |
|---|---|---|---|
| Sebastián Jibaja | 1754265385 | 5 | Coord-05 |
| Karen Mosquera | 1750094144 | 4 | Coord-04 |
| Kened Lugmaña | 1727445866 | 6 | Coord-06 |
| David Jumbo | 1725474447 | 7 | Coord-07 |
| Daniel Mera | 1850629039 | 9 | Coord-09 |
| Leonel Morales | 1752137297 | 7 | Coord-07 |

Coordenadas objetivo: Coord-05, Coord-04, Coord-06, Coord-07, Coord-09

**L = { abcdt+ , abcd* , ab* }**

## Definición formal

```
A = (Q, Σ, δ, q0, F)
Q  = {q0, q1, q2, q3, q4, q5, q6, q7}
Σ  = {a, b, c, d, t}
q0 = q0
F  = {q1, q2, q3, q4, q5, q6, q7}
δ:
   δ(q0, a) = q1
   δ(q1, b) = q2
   δ(q2, b) = q3
   δ(q2, c) = q4
   δ(q3, b) = q3
   δ(q4, d) = q5
   δ(q5, d) = q6
   δ(q5, t) = q7
   δ(q6, d) = q6
   δ(q7, t) = q7
```

## Gramática

```
G = (N, T, P, S)
N = {S, A, B, C, D, E, F, G}
T = {a, b, c, d, t}
S = S
P:
   S → aA | a
   A → bB | b
   B → bC | b | cD | c
   C → bC | b
   D → dE | d
   E → dF | d | tG | t
   F → dF | d
   G → tG | t
```

## Grafo

```mermaid
stateDiagram-v2
    direction LR
    [*] --> q0
    q0 --> q1 : a
    q1 --> q2 : b
    q2 --> q3 : b
    q2 --> q4 : c
    q3 --> q3 : b
    q4 --> q5 : d
    q5 --> q6 : d
    q5 --> q7 : t
    q6 --> q6 : d
    q7 --> q7 : t
    q1 --> [*]
    q2 --> [*]
    q3 --> [*]
    q4 --> [*]
    q5 --> [*]
    q6 --> [*]
    q7 --> [*]
```

Graphviz (pegar en https://dreampuf.github.io/GraphvizOnline):

```dot
digraph AFD {
    rankdir=LR;
    label="L = { abcdt+ , abcd* , ab* }";
    inicio [shape=point];
    q0 [shape=circle];
    q1 [shape=doublecircle];
    q2 [shape=doublecircle];
    q3 [shape=doublecircle];
    q4 [shape=doublecircle];
    q5 [shape=doublecircle];
    q6 [shape=doublecircle];
    q7 [shape=doublecircle];
    inicio -> q0;
    q0 -> q1 [label="a"];
    q1 -> q2 [label="b"];
    q2 -> q3 [label="b"];
    q2 -> q4 [label="c"];
    q3 -> q3 [label="b"];
    q4 -> q5 [label="d"];
    q5 -> q6 [label="d"];
    q5 -> q7 [label="t"];
    q6 -> q6 [label="d"];
    q7 -> q7 [label="t"];
}
```
