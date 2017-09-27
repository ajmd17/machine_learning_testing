/*
 * Copyright (c) 2017 Villu Ruusmann
 *
 * This file is part of JPMML-Evaluator
 *
 * JPMML-Evaluator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Evaluator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Evaluator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.evaluator;

import java.util.List;

public class ReportingDoubleValue
    extends DoubleValue
    implements HasReport
{

    private org.jpmml.evaluator.Report report = null;

    public ReportingDoubleValue(double value, org.jpmml.evaluator.Report report) {
        super(value);
        setReport(report);
        report(new StringBuilder(256).append("<cn>").append(value).append("</cn>").toString());
    }

    public ReportingDoubleValue(double value, org.jpmml.evaluator.Report report, String expression) {
        super(value);
        setReport(report);
        if (expression!= null) {
            report(expression);
        }
    }

    @Override
    public ReportingDoubleValue copy() {
        org.jpmml.evaluator.Report report = getReport();
        return new ReportingDoubleValue(super.value, report.copy(), null);
    }

    @Override
    public ReportingDoubleValue add(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.add(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<cn>").append(value).append("</cn>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue add(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.add(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<cn>").append(value.doubleValue()).append("</cn>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue add(double coefficient, Number factor) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.add(coefficient, factor));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append("<cn>").append(factor.doubleValue()).append("</cn>").append("</apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append("<cn>").append(factor.doubleValue()).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue add(double coefficient, Number factor, int exponent) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.add(coefficient, factor, exponent));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append("<apply><power/>").append("<cn>").append(factor.doubleValue()).append("</cn>").append("<cn>").append(((double) exponent)).append("</cn>").append("</apply></apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append("<apply><power/>").append("<cn>").append(factor.doubleValue()).append("</cn>").append("<cn>").append(((double) exponent)).append("</cn>").append("</apply></apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue add(double coefficient, List<? extends Number> factors) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.add(coefficient, factors));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append(format(factors)).append("</apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(coefficient).append("</cn>").append(format(factors)).append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue subtract(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.subtract(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><minus/>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><minus/>").append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue subtract(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.subtract(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><minus/>").append(getExpression()).append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><minus/>").append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingDoubleValue multiply(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.multiply(value));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue multiply(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.multiply(value));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue multiply(Number factor, double exponent) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.multiply(factor, exponent));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<apply><power/>").append("<cn>").append(factor.doubleValue()).append("</cn>").append("<cn>").append(exponent).append("</cn>").append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue divide(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.divide(value));
        report(new StringBuilder(256).append("<apply><divide/>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue divide(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.divide(value));
        report(new StringBuilder(256).append("<apply><divide/>").append(getExpression()).append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue residual(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.residual(value));
        report(new StringBuilder(256).append("<apply><minus/><cn>1</cn>").append("<cn>").append(value.doubleValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue square() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.square());
        report(new StringBuilder(256).append("<apply><power/>").append(getExpression()).append("<cn>2</cn></apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue reciprocal() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.reciprocal());
        report(new StringBuilder(256).append("<apply><divide/><cn>1</cn>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue elliott() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.elliott());
        report(new StringBuilder(256).append("<apply><ci>elliott</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue exp() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.exp());
        report(new StringBuilder(256).append("<apply><exp/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue gauss() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.gauss());
        report(new StringBuilder(256).append("<apply><ci>gauss</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseLogit() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseLogit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>logit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseCloglog() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseCloglog());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>cloglog</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseLoglog() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseLoglog());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>loglog</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseLogc() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseLogc());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>logc</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseNegbin(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseNegbin(value));
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>negbin</ci></apply>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseOddspower(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseOddspower(value));
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>oddspower</ci></apply>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inversePower(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inversePower(value));
        report(new StringBuilder(256).append("<apply><power/>").append(getExpression()).append("<apply><divide/><cn>1</cn>").append("<cn>").append(value).append("</cn>").append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseCauchit() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseCauchit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>cauchit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue inverseProbit() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.inverseProbit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>probit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue sin() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.sin());
        report(new StringBuilder(256).append("<apply><sin/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue cos() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.cos());
        report(new StringBuilder(256).append("<apply><cos/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue atan() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.atan());
        report(new StringBuilder(256).append("<apply><arctan/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue tanh() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.tanh());
        report(new StringBuilder(256).append("<apply><tanh/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue threshold(double value) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.threshold(value));
        report(new StringBuilder(256).append("<apply><ci>threshold</ci>").append(getExpression()).append("<cn>").append(value).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue relu() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.relu());
        report(new StringBuilder(256).append("<apply><max/><cn>0</cn>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue restrict(double lowValue, double highValue) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.restrict(lowValue, highValue));
        report(new StringBuilder(256).append("<apply><max/>").append("<cn>").append(lowValue).append("</cn>").append("<apply><min/>").append("<cn>").append(highValue).append("</cn>").append(getExpression()).append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue round() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.round());
        report(new StringBuilder(256).append("<apply><ci>round</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue ceiling() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.ceiling());
        report(new StringBuilder(256).append("<apply><ceiling/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue floor() {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.floor());
        report(new StringBuilder(256).append("<apply><floor/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingDoubleValue denormalize(double leftOrig, double leftNorm, double rightOrig, double rightNorm) {
        ReportingDoubleValue result = ((ReportingDoubleValue) super.denormalize(leftOrig, leftNorm, rightOrig, rightNorm));
        report(new StringBuilder(256).append("<apply><ci>denormalize</ci>").append(getExpression()).append("<cn>").append(leftOrig).append("</cn>").append("<cn>").append(leftNorm).append("</cn>").append("<cn>").append(rightOrig).append("</cn>").append("<cn>").append(rightNorm).append("</cn>").append("</apply>").toString());
        return result;
    }

    private void report(String expression) {
        org.jpmml.evaluator.Report report = getReport();
        org.jpmml.evaluator.Report.Entry entry = new org.jpmml.evaluator.Report.Entry(expression, getValue());
        report.add(entry);
    }

    private boolean hasExpression() {
        org.jpmml.evaluator.Report report = getReport();
        return report.hasEntries();
    }

    private String getExpression() {
        org.jpmml.evaluator.Report report = getReport();
        org.jpmml.evaluator.Report.Entry entry = report.tailEntry();
        return entry.getExpression();
    }

    @Override
    public org.jpmml.evaluator.Report getReport() {
        return this.report;
    }

    private void setReport(org.jpmml.evaluator.Report report) {
        this.report = report;
    }

    private static String format(List<? extends Number> values) {
        StringBuilder sb = new StringBuilder((values.size()* 32));
        for (Number value: values) {
            sb.append("<cn>").append(value.doubleValue()).append("</cn>").toString();
        }
        return sb.toString();
    }

}
