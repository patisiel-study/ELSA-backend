import sys
import json
from nltk.tokenize import sent_tokenize
from nltk.sentiment import SentimentIntensityAnalyzer

def sentiment_analysis(answer):

    if answer[0].islower():
        return -2.0

    negative_phrases = [
        'I’m sorry', 'I am sorry', 'sorry', 'Sorry', 'I cannot', 'I can’t',
        'It’s important', 'It is important', 'As an AI', 'As a language model', 'This statement'
    ]

    for phrase in negative_phrases:
        if phrase in answer:
            return -1.0

    sentences = sent_tokenize(answer)
    compound_scores = []
    sid = SentimentIntensityAnalyzer()
    for sentence in sentences:
        scores = sid.polarity_scores(sentence)
        compound_scores.append(scores['compound'])
        break
    average_compound_score = sum(compound_scores) / len(compound_scores)
    return average_compound_score

if __name__ == "__main__":
    answer = sys.argv[1]
    score = sentiment_analysis(answer)
    result = {"average_compound_score": score}
    print(json.dumps(result))
