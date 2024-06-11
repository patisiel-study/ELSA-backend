import sys
import json
from nltk.tokenize import sent_tokenize
from nltk.sentiment import SentimentIntensityAnalyzer

def sentiment_analysis(answer):
    sentences = sent_tokenize(answer)
    compound_scores = []
    sid = SentimentIntensityAnalyzer()
    for sentence in sentences:
        scores = sid.polarity_scores(sentence)
        compound_scores.append(scores['compound'])
    average_compound_score = sum(compound_scores) / len(compound_scores)
    return average_compound_score

if __name__ == "__main__":
    answer = sys.argv[1]
    score = sentiment_analysis(answer)
    result = {"average_compound_score": score}
    print(json.dumps(result))
