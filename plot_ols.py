import pandas
import numpy
from sklearn2pmml import sklearn2pmml
from sklearn_pandas import DataFrameMapper
from sklearn2pmml.decoration import CategoricalDomain, ContinuousDomain
from sklearn.preprocessing import LabelBinarizer, LabelEncoder
# from sklearn.pipeline import Pipeline
from sklearn2pmml import PMMLPipeline
from sklearn.decomposition import PCA
from sklearn.feature_selection import SelectKBest
from sklearn.tree import DecisionTreeClassifier
print numpy.__version__, numpy.__file__
audit_df = pandas.read_csv("./data/Audit.csv")
audit_pipeline = PMMLPipeline([
  ("mapper", DataFrameMapper([
    (["Age", "Income", "Hours"], ContinuousDomain()),
    (["Employment", "Education", "Marital", "Occupation"], CategoricalDomain()),
    (["Gender", "Deductions"], CategoricalDomain())
  ])),
  ("pca", PCA(n_components = 3)),
	("selector", SelectKBest(k = 2)),
  ("classifier", DecisionTreeClassifier(min_samples_split = 10))])

audit_pipeline.fit(audit_df)
# audit_classifier.fit(audit[:, 0:48], audit[:, 48].astype(int))
sklearn2pmml(audit_pipeline, "audit_tree.pmml")