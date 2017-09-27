import pandas
import numpy
from sklearn2pmml import sklearn2pmml
from sklearn_pandas import DataFrameMapper
from sklearn2pmml.decoration import CategoricalDomain, ContinuousDomain
from sklearn.preprocessing import LabelBinarizer, LabelEncoder
# from sklearn.pipeline import Pipeline
# from sklearn2pmml import PMMLPipeline
from sklearn.decomposition import PCA
from sklearn.feature_selection import SelectKBest
from sklearn.tree import DecisionTreeClassifier
print numpy.__version__, numpy.__file__
audit_df = pandas.read_csv("./data/Audit.csv")
audit_mapper = DataFrameMapper([
  (["Age", "Income", "Hours"], ContinuousDomain()),
  (["Employment", "Education", "Marital", "Occupation"], [CategoricalDomain(), LabelBinarizer()]),
  (["Gender", "Deductions"], [CategoricalDomain(), LabelEncoder()]),
  (["Adjusted"], None)])
audit = audit_mapper.fit_transform(audit_df)
audit_classifier = DecisionTreeClassifier(min_samples_split = 10)
audit_classifier.fit(audit[:, 0:48], audit[:, 48].astype(int))
sklearn2pmml(audit_classifier, audit_mapper, "audit_tree.pmml")