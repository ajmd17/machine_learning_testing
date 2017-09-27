import pandas

iris_df = pandas.read_csv("data/Iris.csv")

from sklearn2pmml import PMMLPipeline
from sklearn.tree import DecisionTreeClassifier

iris_pipeline = PMMLPipeline([
	("classifier", DecisionTreeClassifier())
])
iris_pipeline.fit(iris_df[iris_df.columns.difference(["class"])], iris_df["class"])

from sklearn2pmml import sklearn2pmml

sklearn2pmml(iris_pipeline, "DecisionTreeIris.pmml", with_repr = True)