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

public class ReportingFloatValue
    extends FloatValue
    implements HasReport
{

    private org.jpmml.evaluator.Report report = null;

    public ReportingFloatValue(float value, org.jpmml.evaluator.Report report) {
        super(value);
        setReport(report);
        report(new StringBuilder(256).append("<cn>").append(value).append("</cn>").toString());
    }

    public ReportingFloatValue(float value, org.jpmml.evaluator.Report report, String expression) {
        super(value);
        setReport(report);
        if (expression!= null) {
            report(expression);
        }
    }

    @Override
    public ReportingFloatValue copy() {
        org.jpmml.evaluator.Report report = getReport();
        return new ReportingFloatValue(super.value, report.copy(), null);
    }

    @Override
    public ReportingFloatValue add(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.add(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<cn>").append(((float) value)).append("</cn>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue add(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.add(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<cn>").append(value.floatValue()).append("</cn>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue add(double coefficient, Number factor) {
        ReportingFloatValue result = ((ReportingFloatValue) super.add(coefficient, factor));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append("<cn>").append(factor.floatValue()).append("</cn>").append("</apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append("<cn>").append(factor.floatValue()).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue add(double coefficient, Number factor, int exponent) {
        ReportingFloatValue result = ((ReportingFloatValue) super.add(coefficient, factor, exponent));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append("<apply><power/>").append("<cn>").append(factor.floatValue()).append("</cn>").append("<cn>").append(((float) exponent)).append("</cn>").append("</apply></apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append("<apply><power/>").append("<cn>").append(factor.floatValue()).append("</cn>").append("<cn>").append(((float) exponent)).append("</cn>").append("</apply></apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue add(double coefficient, List<? extends Number> factors) {
        ReportingFloatValue result = ((ReportingFloatValue) super.add(coefficient, factors));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><plus/>").append(getExpression()).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append(format(factors)).append("</apply></apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><times/>").append("<cn>").append(((float) coefficient)).append("</cn>").append(format(factors)).append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue subtract(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.subtract(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><minus/>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><minus/>").append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue subtract(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.subtract(value));
        if (hasExpression()) {
            report(new StringBuilder(256).append("<apply><minus/>").append(getExpression()).append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        } else {
            report(new StringBuilder(256).append("<apply><minus/>").append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        }
        return result;
    }

    @Override
    public ReportingFloatValue multiply(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.multiply(value));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue multiply(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.multiply(value));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue multiply(Number factor, double exponent) {
        ReportingFloatValue result = ((ReportingFloatValue) super.multiply(factor, exponent));
        report(new StringBuilder(256).append("<apply><times/>").append(getExpression()).append("<apply><power/>").append("<cn>").append(factor.floatValue()).append("</cn>").append("<cn>").append(((float) exponent)).append("</cn>").append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue divide(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.divide(value));
        report(new StringBuilder(256).append("<apply><divide/>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue divide(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.divide(value));
        report(new StringBuilder(256).append("<apply><divide/>").append(getExpression()).append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue residual(org.jpmml.evaluator.Value<? extends Number> value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.residual(value));
        report(new StringBuilder(256).append("<apply><minus/><cn>1</cn>").append("<cn>").append(value.floatValue()).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue square() {
        ReportingFloatValue result = ((ReportingFloatValue) super.square());
        report(new StringBuilder(256).append("<apply><power/>").append(getExpression()).append("<cn>2</cn></apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue reciprocal() {
        ReportingFloatValue result = ((ReportingFloatValue) super.reciprocal());
        report(new StringBuilder(256).append("<apply><divide/><cn>1</cn>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue elliott() {
        ReportingFloatValue result = ((ReportingFloatValue) super.elliott());
        report(new StringBuilder(256).append("<apply><ci>elliott</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue exp() {
        ReportingFloatValue result = ((ReportingFloatValue) super.exp());
        report(new StringBuilder(256).append("<apply><exp/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue gauss() {
        ReportingFloatValue result = ((ReportingFloatValue) super.gauss());
        report(new StringBuilder(256).append("<apply><ci>gauss</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseLogit() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseLogit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>logit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseCloglog() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseCloglog());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>cloglog</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseLoglog() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseLoglog());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>loglog</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseLogc() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseLogc());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>logc</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseNegbin(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseNegbin(value));
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>negbin</ci></apply>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseOddspower(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseOddspower(value));
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>oddspower</ci></apply>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inversePower(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.inversePower(value));
        report(new StringBuilder(256).append("<apply><power/>").append(getExpression()).append("<apply><divide/><cn>1</cn>").append("<cn>").append(((float) value)).append("</cn>").append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseCauchit() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseCauchit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>cauchit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue inverseProbit() {
        ReportingFloatValue result = ((ReportingFloatValue) super.inverseProbit());
        report(new StringBuilder(256).append("<apply><apply><inverse/><ci>probit</ci></apply>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue sin() {
        ReportingFloatValue result = ((ReportingFloatValue) super.sin());
        report(new StringBuilder(256).append("<apply><sin/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue cos() {
        ReportingFloatValue result = ((ReportingFloatValue) super.cos());
        report(new StringBuilder(256).append("<apply><cos/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue atan() {
        ReportingFloatValue result = ((ReportingFloatValue) super.atan());
        report(new StringBuilder(256).append("<apply><arctan/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue tanh() {
        ReportingFloatValue result = ((ReportingFloatValue) super.tanh());
        report(new StringBuilder(256).append("<apply><tanh/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue threshold(double value) {
        ReportingFloatValue result = ((ReportingFloatValue) super.threshold(value));
        report(new StringBuilder(256).append("<apply><ci>threshold</ci>").append(getExpression()).append("<cn>").append(((float) value)).append("</cn>").append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue relu() {
        ReportingFloatValue result = ((ReportingFloatValue) super.relu());
        report(new StringBuilder(256).append("<apply><max/><cn>0</cn>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue restrict(double lowValue, double highValue) {
        ReportingFloatValue result = ((ReportingFloatValue) super.restrict(lowValue, highValue));
        report(new StringBuilder(256).append("<apply><max/>").append("<cn>").append(((float) lowValue)).append("</cn>").append("<apply><min/>").append("<cn>").append(((float) highValue)).append("</cn>").append(getExpression()).append("</apply></apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue round() {
        ReportingFloatValue result = ((ReportingFloatValue) super.round());
        report(new StringBuilder(256).append("<apply><ci>round</ci>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue ceiling() {
        ReportingFloatValue result = ((ReportingFloatValue) super.ceiling());
        report(new StringBuilder(256).append("<apply><ceiling/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue floor() {
        ReportingFloatValue result = ((ReportingFloatValue) super.floor());
        report(new StringBuilder(256).append("<apply><floor/>").append(getExpression()).append("</apply>").toString());
        return result;
    }

    @Override
    public ReportingFloatValue denormalize(double leftOrig, double leftNorm, double rightOrig, double rightNorm) {
        ReportingFloatValue result = ((ReportingFloatValue) super.denormalize(leftOrig, leftNorm, rightOrig, rightNorm));
        report(new StringBuilder(256).append("<apply><ci>denormalize</ci>").append(getExpression()).append("<cn>").append(((float) leftOrig)).append("</cn>").append("<cn>").append(((float) leftNorm)).append("</cn>").append("<cn>").append(((float) rightOrig)).append("</cn>").append("<cn>").append(((float) rightNorm)).append("</cn>").append("</apply>").toString());
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
            sb.append("<cn>").append(value.floatValue()).append("</cn>").toString();
        }
        return sb.toString();
    }

}
