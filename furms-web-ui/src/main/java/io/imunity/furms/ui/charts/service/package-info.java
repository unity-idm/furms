/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */


/**
 * This package used to produce data, which are readable for ApexCharts(lib generating charts)
 * Firs of all FURMS use only line charts, so code based on simple concept of line function f(x) = y.
 * So we need to generate x which will be arguments of function and y which will be values of function.
 * FURMS use chart with many lines, so we need to generate a couple of line functions: f(x1) = y1, f(x2) = y2, ..., f(xn) = yn,
 * but ApexCharts takes only one list of x arguments and many list of y values.
 * So we have to make a sum collection of x arguments from all functions.
 * Summarise we have to generate one list of x arguments and a couple of list of y values,
 * depending on lines amount. ApexCharts takes individual list of arguments and individual list of values,
 * so the order of the list elements matter and have to be compatible with line function.
 * Because all lists of y values used the same list of x arguments, sometimes we have to repeat value if based
 * function doesn't have particular x argument. It's hard concept so the best way to understand it is an example.
 * Base functions: f1(1) = 2, f1(3) = 5; f2(2) = 1, f(4) = 5
 * Sum of x arguments: {1,2,3,4}
 * After fit functions to ApexCharts: f1(1) = 2, f1(2) = 2, f1(3) = 5, f1(4) = 5; f2(1) = 0, f2(2) = 1, f1(2) = 1, f(4) = 5
 * In this package x arguments are LocalDate type and y vales are Double type.
 */
package io.imunity.furms.ui.charts.service;