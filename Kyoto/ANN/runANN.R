rm(list = ls(all = TRUE))

install.packages('neuralnet')
install.packages("hydroGOF")

library("neuralnet")
library("hydroGOF")


dir0 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/Retroativo"
dir1 = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/OUTPUTS/PERSISTENCE/Retroativo"

########################################################################
##set globle parameters
forecast.start = "2013-12-01"
data.interval = 15
window_size = 13
########################################################################

setwd(dir0)
files = dir()

data = NULL
for(x in files)
{
  temp = read.table(x, header = TRUE, sep = "\t")
  data = rbind(data,temp)
  print(x)
}

Tm = as.POSIXlt(data$Tm, tz = "GMT")

G = data[,2]
G = (G-min(G))/(max(G)-min(G))


G_input = NULL
G_output = NULL


for(i in 1:(length(G)-window_size))
{
  G_input = rbind(G_input,t(G[i:(i+window_size-1)]))
  G_output = rbind(G_output, G[(i+window_size)])
}

data = data.frame(G_input, G_output)
data = data[complete.cases(data),] # remove missing points

train_size = round(length(G) * 0.8)

traininginput <-  as.data.frame(G_input[1:train_size,])
trainingoutput <- data$G_output[1:train_size]

#Column bind the data into one variable
trainingdata <- cbind(traininginput,trainingoutput)
trainingdata = data[complete.cases(trainingdata),]

colnames(trainingdata) <- c("G1","G2","G3","G4","G5","G6","G7","G8","G9","G10","G11","G12","G13","Output")

#Train the neural network
#Going to have 10 hidden layers
#Threshold is a numeric value specifying the threshold for the partial
#derivatives of the error function as stopping criteria.
net.sqrt <- neuralnet(Output~G1+G2+G3+G4+G5+G6+G7+G8+G9+G10+G11+G12+G13,trainingdata, hidden=20, threshold=0.1,
                      algorithm='rprop+',err.fct='sse', act.fct='tanh', linear.output=FALSE)
print(net.sqrt)

#Plot the neural network
plot(net.sqrt)

#Test the neural network on some training data
testinput <-  as.data.frame(G_input[(train_size+1):nrow(G_input),])
validateoutput <- G_output[(train_size+1):length(G_output)]

net.results <- compute(net.sqrt, testinput) #Run them through the neural network

#Lets see what properties net.sqrt has
ls(net.results)

#Lets see the results
print(net.results$net.result)

nrmse(net.results$net.result[,1], validateoutput)